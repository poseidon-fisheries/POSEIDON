/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.ImmutableMap;
import com.univocity.parsers.common.record.Record;
import com.vividsolutions.jts.geom.Coordinate;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.ActionWeightsCache;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.DurationSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.PurseSeinerActionClassToDouble;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.oxfish.utility.operators.LogisticFunctionFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.geography.currents.CurrentVectorsFactory.metrePerSecondToXyPerDaysVector;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public abstract class PurseSeinerFishingStrategyFactory<B extends LocalBiology>
    implements AlgorithmFactory<PurseSeinerFishingStrategy<B>>, Dummyable {

    private static final ActiveOpportunitiesFactory activeOpportunitiesFactory =
        new ActiveOpportunitiesFactory();
    private static final CacheByFishState<ActiveOpportunities> activeDolphinSetOpportunitiesCache =
        new CacheByFishState<>(activeOpportunitiesFactory);
    private static final CacheByFishState<ActiveOpportunities>
        activeNonAssociatedSetOpportunitiesCache =
        new CacheByFishState<>(activeOpportunitiesFactory);
    private final boolean noaSetsCanPoachFads = false;
    private final boolean delSetsCanPoachFads = false;
    private final int noaSetsRangeInSeaTiles = 0;
    private final int delSetsRangeInSeaTiles = 0;
    private final Class<? extends B> biologyClass;
    private IntegerParameter targetYear;
    private SetDurationSamplersFactory setDurationSamplersFactory;
    private InputPath actionWeightsFile;
    private CatchSamplersFactory<B> catchSamplersFactory;
    private InputPath setCompositionWeightsFile;
    // use default values from:
    // https://github.com/poseidon-fisheries/tuna/blob/9c6f775ced85179ec39e12d8a0818bfcc2fbc83f/calibration/results/ernesto/best_base_line/calibrated_scenario.yaml
    private double searchBonus = 0.1;
    private double nonAssociatedSetDetectionProbability = 1.0;
    private double dolphinSetDetectionProbability = 0.7136840195385347;
    private double opportunisticFadSetDetectionProbability = 0.007275362250433118;
    private double searchActionDecayConstant = 7.912472944827373;
    private double fadDeploymentActionDecayConstant = 0.7228626294613664;
    private double movingThreshold = 0.0;
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        nonAssociatedSetGeneratorFunction =
        new LogisticFunctionFactory(15392.989688872976, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        dolphinSetGeneratorFunction =
        new LogisticFunctionFactory(EPSILON, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        searchActionValueFunction =
        new LogisticFunctionFactory(7081017.137484187, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        fadDeploymentActionValueFunction =
        new LogisticFunctionFactory(7338176.765769132, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        fadSetActionValueFunction =
        new LogisticFunctionFactory(EPSILON, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        opportunisticFadSetActionValueFunction =
        new LogisticFunctionFactory(EPSILON, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        nonAssociatedSetActionValueFunction =
        new LogisticFunctionFactory(555715.859646539, 10);
    private AlgorithmFactory<? extends DoubleUnaryOperator>
        dolphinSetActionValueFunction =
        new LogisticFunctionFactory(1E-6, 10);
    private InputPath maxCurrentSpeedsFile;

    PurseSeinerFishingStrategyFactory(
        final IntegerParameter targetYear,
        final Class<? extends B> biologyClass,
        final InputPath actionWeightsFile,
        final CatchSamplersFactory<B> catchSamplersFactory,
        final SetDurationSamplersFactory setDurationSamplersFactory,
        final InputPath maxCurrentSpeedsFile,
        final InputPath setCompositionWeightsFile
    ) {
        this(biologyClass);
        this.targetYear = targetYear;
        this.actionWeightsFile = actionWeightsFile;
        this.catchSamplersFactory = catchSamplersFactory;
        this.setDurationSamplersFactory = setDurationSamplersFactory;
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
        this.setCompositionWeightsFile = setCompositionWeightsFile;
    }

    PurseSeinerFishingStrategyFactory(
        final Class<? extends B> biologyClass
    ) {
        this.biologyClass = biologyClass;
    }

    public static Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> loadActionWeights(
        final int targetYear,
        final Path attractionWeightsFile
    ) {
        return fisher -> stream(ActionClass.values())
            .map(ActionClass::getActionClass)
            .collect(toImmutableMap(
                identity(),
                actionClass -> ActionWeightsCache.INSTANCE.get(
                    attractionWeightsFile,
                    targetYear,
                    fisher,
                    actionClass
                )
            ));
    }

    @SuppressWarnings("unused")
    public SetDurationSamplersFactory getSetDurationSamplersFactory() {
        return setDurationSamplersFactory;
    }

    @SuppressWarnings("unused")
    public void setSetDurationSamplersFactory(final SetDurationSamplersFactory setDurationSamplersFactory) {
        this.setDurationSamplersFactory = setDurationSamplersFactory;
    }

    @SuppressWarnings("unused")
    public boolean isNoaSetsCanPoachFads() {
        return noaSetsCanPoachFads;
    }

    @SuppressWarnings("unused")
    public boolean isDelSetsCanPoachFads() {
        return delSetsCanPoachFads;
    }

    @SuppressWarnings("unused")
    public int getNoaSetsRangeInSeaTiles() {
        return noaSetsRangeInSeaTiles;
    }

    @SuppressWarnings("unused")
    public int getDelSetsRangeInSeaTiles() {
        return delSetsRangeInSeaTiles;
    }

    @SuppressWarnings("unused")
    public InputPath getMaxCurrentSpeedsFile() {
        return maxCurrentSpeedsFile;
    }

    @SuppressWarnings("unused")
    public void setMaxCurrentSpeedsFile(final InputPath maxCurrentSpeedsFile) {
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetGeneratorFunction() {
        return nonAssociatedSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetGeneratorFunction(
        final AlgorithmFactory<? extends DoubleUnaryOperator> nonAssociatedSetGeneratorFunction
    ) {
        this.nonAssociatedSetGeneratorFunction = nonAssociatedSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetGeneratorFunction() {
        return dolphinSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetGeneratorFunction(
        final AlgorithmFactory<? extends DoubleUnaryOperator> dolphinSetGeneratorFunction
    ) {
        this.dolphinSetGeneratorFunction = dolphinSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getSearchActionValueFunction() {
        return searchActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setSearchActionValueFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> searchActionValueFunction) {
        this.searchActionValueFunction = searchActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getFadDeploymentActionValueFunction() {
        return fadDeploymentActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentActionValueFunction(
        final AlgorithmFactory<? extends DoubleUnaryOperator> fadDeploymentActionValueFunction
    ) {
        this.fadDeploymentActionValueFunction = fadDeploymentActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getFadSetActionValueFunction() {
        return fadSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setFadSetActionValueFunction(final AlgorithmFactory<? extends DoubleUnaryOperator> fadSetActionValueFunction) {
        this.fadSetActionValueFunction = fadSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getOpportunisticFadSetActionValueFunction() {
        return opportunisticFadSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetActionValueFunction(
        final AlgorithmFactory<? extends DoubleUnaryOperator> opportunisticFadSetActionValueFunction
    ) {
        this.opportunisticFadSetActionValueFunction = opportunisticFadSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetActionValueFunction() {
        return nonAssociatedSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetActionValueFunction(
        final AlgorithmFactory<? extends DoubleUnaryOperator> nonAssociatedSetActionValueFunction
    ) {
        this.nonAssociatedSetActionValueFunction = nonAssociatedSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetActionValueFunction() {
        return dolphinSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetActionValueFunction(
        final AlgorithmFactory<? extends DoubleUnaryOperator> dolphinSetActionValueFunction
    ) {
        this.dolphinSetActionValueFunction = dolphinSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public InputPath getActionWeightsFile() {
        return actionWeightsFile;
    }

    @SuppressWarnings("unused")
    public void setActionWeightsFile(final InputPath actionWeightsFile) {
        this.actionWeightsFile = actionWeightsFile;
    }

    @SuppressWarnings("unused")
    public CatchSamplersFactory<B> getCatchSamplersFactory() {
        return catchSamplersFactory;
    }

    @SuppressWarnings("unused")
    public void setCatchSamplersFactory(final CatchSamplersFactory<B> catchSamplersFactory) {
        this.catchSamplersFactory = catchSamplersFactory;
    }

    @SuppressWarnings("unused")
    public double getMovingThreshold() {
        return movingThreshold;
    }

    @SuppressWarnings("unused")
    public void setMovingThreshold(final double movingThreshold) {
        this.movingThreshold = movingThreshold;
    }

    @SuppressWarnings("unused")
    public double getSearchActionDecayConstant() {
        return searchActionDecayConstant;
    }

    @SuppressWarnings("unused")
    public void setSearchActionDecayConstant(final double searchActionDecayConstant) {
        this.searchActionDecayConstant = searchActionDecayConstant;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetDetectionProbability() {
        return opportunisticFadSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetDetectionProbability(
        final double opportunisticFadSetDetectionProbability
    ) {
        checkArgument(opportunisticFadSetDetectionProbability >= 0
            && opportunisticFadSetDetectionProbability <= 1);
        this.opportunisticFadSetDetectionProbability = opportunisticFadSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetDetectionProbability() {
        return nonAssociatedSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetDetectionProbability(
        final double nonAssociatedSetDetectionProbability
    ) {
        checkArgument(
            nonAssociatedSetDetectionProbability >= 0 && nonAssociatedSetDetectionProbability <= 1);
        this.nonAssociatedSetDetectionProbability = nonAssociatedSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetDetectionProbability() {
        return dolphinSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetDetectionProbability(final double dolphinSetDetectionProbability) {
        checkArgument(dolphinSetDetectionProbability >= 0 && dolphinSetDetectionProbability <= 1);
        this.dolphinSetDetectionProbability = dolphinSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public double getSearchBonus() {
        return searchBonus;
    }

    @SuppressWarnings("unused")
    public void setSearchBonus(final double searchBonus) {
        this.searchBonus = searchBonus;
    }

    @Override
    public PurseSeinerFishingStrategy<B> apply(final FishState fishState) {
        checkNotNull(catchSamplersFactory);
        checkNotNull(actionWeightsFile);
        checkNotNull(maxCurrentSpeedsFile);
        return callConstructor(
            this::loadActionWeights,
            this::makeSetOpportunityDetector,
            makeActionValueFunctions(fishState),
            getMaxCurrentSpeeds(fishState.getMap()),
            searchActionDecayConstant,
            fadDeploymentActionDecayConstant,
            movingThreshold
        );
    }

    protected PurseSeinerFishingStrategy<B> callConstructor(
        final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> attractionWeights,
        final Function<Fisher, SetOpportunityDetector<B>> opportunityDetector,
        final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions,
        final ToDoubleFunction<Class<? extends PurseSeinerAction>> maxCurrentSpeeds,
        final double searchActionDecayConstant,
        final double fadDeploymentActionDecayConstant,
        final double movingThreshold
    ) {
        return new PurseSeinerFishingStrategy<>(
            attractionWeights,
            opportunityDetector,
            actionValueFunctions,
            maxCurrentSpeeds,
            searchActionDecayConstant,
            fadDeploymentActionDecayConstant,
            movingThreshold
        );
    }

    private Map<Class<? extends PurseSeinerAction>, Double> loadActionWeights(
        final Fisher fisher
    ) {
        return stream(ActionClass.values())
            .map(ActionClass::getActionClass)
            .collect(toImmutableMap(
                identity(),
                actionClass -> ActionWeightsCache.INSTANCE.get(
                    actionWeightsFile.get(),
                    targetYear.getValue(),
                    fisher,
                    actionClass
                )
            ));
    }

    private SetOpportunityDetector<B> makeSetOpportunityDetector(final Fisher fisher) {

        final FishState fishState = fisher.grabState();

        final Map<Class<? extends PurseSeinerAction>, Map<Species, Double>>
            setCompositionWeights = loadSetCompositionWeights(fishState);

        final Map<Class<? extends AbstractSetAction>, DurationSampler> durationSamplers =
            setDurationSamplersFactory.apply(fishState);

        final FadSetOpportunityGenerator<B, ?, FadSetAction>
            fadSetOpportunityGenerator =
            new FadSetOpportunityGenerator<>(
                (fisher1, fad) -> fad.getOwner() == getFadManager(fisher1),
                FadSetAction::new,
                durationSamplers.get(FadSetAction.class)
            );

        final FadSetOpportunityGenerator<B, ?, OpportunisticFadSetAction>
            opportunisticFadSetOpportunityGenerator =
            new FadSetOpportunityGenerator<>(
                (fisher1, fad) -> fad.getOwner() != getFadManager(fisher1),
                OpportunisticFadSetAction::new,
                durationSamplers.get(FadSetAction.class)
            );

        final CatchMaker<B> catchMaker = getCatchMaker(fishState.getBiology());

        final TargetBiologiesGrabber<B> noaBiologyGrabber = new TargetBiologiesGrabber<>(
            noaSetsCanPoachFads,
            noaSetsRangeInSeaTiles,
            biologyClass
        );

        final SchoolSetOpportunityGenerator<B, NonAssociatedSetAction<B>>
            nonAssociatedSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<>(
                nonAssociatedSetGeneratorFunction.apply(fishState),
                setCompositionWeights.get(NonAssociatedSetAction.class),
                catchSamplersFactory.apply(fishState).get(NonAssociatedSetAction.class),
                new NonAssociatedSetActionMaker<>(catchMaker),
                activeNonAssociatedSetOpportunitiesCache.get(fishState),
                durationSamplers.get(NonAssociatedSetAction.class),
                noaBiologyGrabber
            );

        final TargetBiologiesGrabber<B> delBiologyGrabber = new TargetBiologiesGrabber<>(
            delSetsCanPoachFads,
            delSetsRangeInSeaTiles,
            biologyClass
        );
        final SchoolSetOpportunityGenerator<B, DolphinSetAction<B>>
            dolphinSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<>(
                dolphinSetGeneratorFunction.apply(fishState),
                setCompositionWeights.get(DolphinSetAction.class),
                catchSamplersFactory.apply(fishState).get(DolphinSetAction.class),
                new DolphinSetActionMaker<>(catchMaker),
                activeDolphinSetOpportunitiesCache.get(fishState),
                durationSamplers.get(DolphinSetAction.class),
                delBiologyGrabber
            );

        return new SetOpportunityDetector<>(
            fisher,
            ImmutableMap.of(
                fadSetOpportunityGenerator, 1.0,
                opportunisticFadSetOpportunityGenerator, opportunisticFadSetDetectionProbability,
                nonAssociatedSetOpportunityGenerator, nonAssociatedSetDetectionProbability,
                dolphinSetOpportunityGenerator, dolphinSetDetectionProbability
            ),
            searchBonus
        );
    }

    private Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator>
    makeActionValueFunctions(final FishState fishState) {
        return new ImmutableMap.Builder<Class<? extends PurseSeinerAction>, DoubleUnaryOperator>()
            .put(
                SearchAction.class,
                searchActionValueFunction.apply(fishState)
            )
            .put(
                FadDeploymentAction.class,
                fadDeploymentActionValueFunction.apply(fishState)
            )
            .put(
                NonAssociatedSetAction.class,
                nonAssociatedSetActionValueFunction.apply(fishState)
            )
            .put(
                DolphinSetAction.class,
                dolphinSetActionValueFunction.apply(fishState)
            )
            .put(
                FadSetAction.class,
                fadSetActionValueFunction.apply(fishState)
            )
            .put(
                OpportunisticFadSetAction.class,
                opportunisticFadSetActionValueFunction.apply(fishState)
            )
            .build();
    }

    /**
     * Convert the max current speeds in m/s per seconds given in the input file into degrees per day. For the purpose
     * of the conversion, we assume that we're at the equator. This means that the max speeds we calculate in °/day
     * represent lower speeds in m/s as we move away from the equator, and thus that fishers are slightly less tolerant
     * of strong currents away from the equator but the difference is small enough to ignore and doing thing the right
     * way would massively complicate things.
     */
    private PurseSeinerActionClassToDouble getMaxCurrentSpeeds(final NauticalMap nauticalMap) {
        final Coordinate coordinate = new Coordinate(0, 0);
        final MapExtent mapExtent = nauticalMap.getMapExtent();
        return PurseSeinerActionClassToDouble
            .fromFile(
                maxCurrentSpeedsFile.get(),
                getTargetYear().getValue(),
                "action",
                "speed"
            )
            .mapValues(speed ->
                metrePerSecondToXyPerDaysVector(
                    new Double2D(speed, 0),
                    coordinate,
                    mapExtent
                ).length()
            );
    }

    private Map<Class<? extends PurseSeinerAction>, Map<Species, Double>> loadSetCompositionWeights(
        final FishState fishState
    ) {
        return recordStream(setCompositionWeightsFile.get())
            .collect(groupingBy(r -> ActionClass.valueOf(r.getString("set_type"))
                .getActionClass()))
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> makeWeightMap(fishState, entry.getValue(), getTargetYear().getValue())
            ));
    }

    abstract CatchMaker<B> getCatchMaker(GlobalBiology globalBiology);

    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    private ImmutableMap<Species, Double> makeWeightMap(
        final FishState fishState,
        final Collection<? extends Record> records,
        final int targetYear
    ) {
        return records.stream()
            .filter(r -> r.getInt("year") == targetYear)
            .collect(toImmutableMap(
                r -> fishState.getBiology().getSpeciesByCode(r.getString("species_code").toUpperCase()),
                r -> r.getDouble("weight")
            ));
    }

    @SuppressWarnings("unused")
    public InputPath getSetCompositionWeightsFile() {
        return setCompositionWeightsFile;
    }

    @SuppressWarnings("unused")
    public void setSetCompositionWeightsFile(final InputPath setCompositionWeightsFile) {
        this.setCompositionWeightsFile = setCompositionWeightsFile;
    }

    @SuppressWarnings("unused")
    public double getFadDeploymentActionDecayConstant() {
        return fadDeploymentActionDecayConstant;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentActionDecayConstant(final double fadDeploymentActionDecayConstant) {
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        actionWeightsFile = dummyDataFolder.path("dummy_action_weights.csv");
    }
}
