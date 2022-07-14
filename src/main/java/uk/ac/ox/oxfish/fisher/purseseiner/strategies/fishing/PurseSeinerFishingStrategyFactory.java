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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.collect.ImmutableMap;
import com.univocity.parsers.common.record.Record;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.tuna.Aggregator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.CatchMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.OpportunisticFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.SearchAction;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.ActionWeightsCache;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.FisherValuesByActionFromFileCache.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.DurationSampler;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.SetDurationSamplersFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.PurseSeinerActionClassToDouble;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.operators.LogisticFunctionFactory;

public abstract class PurseSeinerFishingStrategyFactory<B extends LocalBiology, F extends Fad<B, F>>
    implements AlgorithmFactory<PurseSeinerFishingStrategy<B>> {

    private static final ActiveOpportunitiesFactory activeOpportunitiesFactory =
        new ActiveOpportunitiesFactory();
    private static final CacheByFishState<ActiveOpportunities> activeDolphinSetOpportunitiesCache =
        new CacheByFishState<>(activeOpportunitiesFactory);
    private static final CacheByFishState<ActiveOpportunities>
        activeNonAssociatedSetOpportunitiesCache =
        new CacheByFishState<>(activeOpportunitiesFactory);
    private final SetDurationSamplersFactory setDurationSamplers = new SetDurationSamplersFactory();
    private final CacheByFishState<Map<Class<? extends AbstractSetAction<?>>, DurationSampler>>
        setDurationSamplersCache = new CacheByFishState<>(setDurationSamplers);
    private final Class<F> fadClass;
    private final Class<B> biologyClass;
    private final SpeciesCodes speciesCodes = EpoScenario.speciesCodesSupplier.get();
    private Path attractionWeightsFile;
    private CatchSamplersFactory<B> catchSamplersFactory;
    private Path setCompositionWeightsPath = INPUT_PATH.resolve("set_compositions.csv");

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
    private boolean fishUnderFadsAvailableForSchoolSets = true;
    private Path maxCurrentSpeedsFile = INPUT_PATH.resolve("max_current_speeds.csv");

    PurseSeinerFishingStrategyFactory(
        final Class<B> biologyClass,
        final Class<F> fadClass
    ) {
        this.fadClass = fadClass;
        this.biologyClass = biologyClass;
    }

    @SuppressWarnings("unused")
    public Path getMaxCurrentSpeedsFile() {
        return maxCurrentSpeedsFile;
    }

    @SuppressWarnings("unused")
    public void setMaxCurrentSpeedsFile(final Path maxCurrentSpeedsFile) {
        this.maxCurrentSpeedsFile = maxCurrentSpeedsFile;
    }

    @SuppressWarnings("unused")
    public boolean isFishUnderFadsAvailableForSchoolSets() {
        return fishUnderFadsAvailableForSchoolSets;
    }

    @SuppressWarnings("unused")
    public void setFishUnderFadsAvailableForSchoolSets(final boolean fishUnderFadsAvailableForSchoolSets) {
        this.fishUnderFadsAvailableForSchoolSets = fishUnderFadsAvailableForSchoolSets;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetGeneratorFunction() {
        return nonAssociatedSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetGeneratorFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> nonAssociatedSetGeneratorFunction
    ) {
        this.nonAssociatedSetGeneratorFunction = nonAssociatedSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetGeneratorFunction() {
        return dolphinSetGeneratorFunction;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetGeneratorFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> dolphinSetGeneratorFunction
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
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> fadDeploymentActionValueFunction
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
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> opportunisticFadSetActionValueFunction
    ) {
        this.opportunisticFadSetActionValueFunction = opportunisticFadSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getNonAssociatedSetActionValueFunction() {
        return nonAssociatedSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetActionValueFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> nonAssociatedSetActionValueFunction
    ) {
        this.nonAssociatedSetActionValueFunction = nonAssociatedSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<? extends DoubleUnaryOperator> getDolphinSetActionValueFunction() {
        return dolphinSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetActionValueFunction(
        final AlgorithmFactory<?
            extends DoubleUnaryOperator> dolphinSetActionValueFunction
    ) {
        this.dolphinSetActionValueFunction = dolphinSetActionValueFunction;
    }

    @SuppressWarnings("unused")
    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    @SuppressWarnings("unused")
    public CatchSamplersFactory<B> getCatchSamplersFactory() {
        return catchSamplersFactory;
    }

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
        checkNotNull(attractionWeightsFile);
        checkNotNull(maxCurrentSpeedsFile);
        return callConstructor(
            this::loadAttractionWeights,
            this::makeSetOpportunityDetector,
            makeActionValueFunctions(fishState),
            PurseSeinerActionClassToDouble.fromFile(maxCurrentSpeedsFile, "action", "speed"),
            searchActionDecayConstant,
            fadDeploymentActionDecayConstant,
            movingThreshold
        );
    }

    @NotNull
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

    private Map<Class<? extends PurseSeinerAction>, Double> loadAttractionWeights(
        final Fisher fisher
    ) {
        return stream(ActionClass.values())
            .map(ActionClass::getActionClass)
            .collect(toImmutableMap(
                identity(),
                actionClass -> ActionWeightsCache.INSTANCE.get(
                    attractionWeightsFile,
                    TARGET_YEAR,
                    fisher,
                    actionClass
                )
            ));
    }

    public static Function<Fisher,
            Map<Class<? extends PurseSeinerAction>, Double>> loadAttractionWeights(
            Path attractionWeightsFile
    ) {
        return fisher -> stream(ActionClass.values())
                .map(ActionClass::getActionClass)
                .collect(toImmutableMap(
                        identity(),
                        actionClass -> ActionWeightsCache.INSTANCE.get(
                                attractionWeightsFile,
                                TARGET_YEAR,
                                fisher,
                                actionClass
                        )
                ));


    }


    private SetOpportunityDetector<B> makeSetOpportunityDetector(final Fisher fisher) {

        final FishState fishState = fisher.grabState();

        final ImmutableMap<Class<? extends PurseSeinerAction>, ImmutableMap<Species, Double>>
            setCompositionWeights = loadSetCompositionWeights(fishState);

        final Map<Class<? extends AbstractSetAction<?>>, DurationSampler> durationSamplers =
            setDurationSamplersCache.get(fishState);
        final FadSetOpportunityGenerator<B, F, FadSetAction<B, F>>
            fadSetOpportunityGenerator =
            new FadSetOpportunityGenerator<>(
                fadClass,
                (fisher1, fad) -> fad.getOwner() == getFadManager(fisher1),
                FadSetAction<B, F>::new,
                durationSamplers.get(FadSetAction.class)
            );

        final FadSetOpportunityGenerator<B, F, OpportunisticFadSetAction<B, F>>
            opportunisticFadSetOpportunityGenerator =
            new FadSetOpportunityGenerator<>(
                fadClass,
                (fisher1, fad) -> fad.getOwner() != getFadManager(fisher1),
                OpportunisticFadSetAction<B, F>::new,
                durationSamplers.get(FadSetAction.class)
            );

        final CatchMaker<B> catchMaker = getCatchMaker(fishState.getBiology());
        final SchoolSetOpportunityGenerator<B, NonAssociatedSetAction<B>>
            nonAssociatedSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<>(
                nonAssociatedSetGeneratorFunction.apply(fishState),
                setCompositionWeights.get(NonAssociatedSetAction.class),
                biologyClass,
                catchSamplersFactory.apply(fishState).get(NonAssociatedSetAction.class),
                new NonAssociatedSetActionMaker<>(catchMaker),
                activeNonAssociatedSetOpportunitiesCache.get(fishState),
                durationSamplers.get(NonAssociatedSetAction.class),
                getBiologyAggregator(),
                fishUnderFadsAvailableForSchoolSets
            );

        final SchoolSetOpportunityGenerator<B, DolphinSetAction<B>>
            dolphinSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<>(
                dolphinSetGeneratorFunction.apply(fishState),
                setCompositionWeights.get(DolphinSetAction.class),
                biologyClass,
                catchSamplersFactory.apply(fishState).get(DolphinSetAction.class),
                new DolphinSetActionMaker<>(catchMaker),
                activeDolphinSetOpportunitiesCache.get(fishState),
                durationSamplers.get(DolphinSetAction.class),
                getBiologyAggregator(),
                fishUnderFadsAvailableForSchoolSets
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

    abstract Aggregator<B> getBiologyAggregator();

    abstract CatchMaker<B> getCatchMaker(GlobalBiology globalBiology);

    private ImmutableMap<Class<? extends PurseSeinerAction>, ImmutableMap<Species, Double>>
    loadSetCompositionWeights(
        final FishState fishState
    ) {
        return parseAllRecords(setCompositionWeightsPath)
            .stream()
            .collect(groupingBy(r -> ActionClass.valueOf(r.getString("set_type"))
                .getActionClass()))
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> makeWeightMap(fishState, entry.getValue())
            ));
    }

    private ImmutableMap<Species, Double> makeWeightMap(
        final FishState fishState,
        final Collection<Record> records
    ) {
        return
            records.stream().collect(toImmutableMap(
                r -> {
                    final String speciesCode = r.getString("species_code").toUpperCase();
                    final String speciesName = speciesCodes.getSpeciesName(speciesCode);
                    return fishState.getBiology().getSpecie(speciesName);
                },
                r -> r.getDouble("weight")
            ));

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

    @SuppressWarnings("unused")
    public Path getSetCompositionWeightsPath() {
        return setCompositionWeightsPath;
    }

    @SuppressWarnings("unused")
    public void setSetCompositionWeightsPath(final Path setCompositionWeightsPath) {
        this.setCompositionWeightsPath = setCompositionWeightsPath;
    }

    @SuppressWarnings("unused")
    public double getFadDeploymentActionDecayConstant() {
        return fadDeploymentActionDecayConstant;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentActionDecayConstant(final double fadDeploymentActionDecayConstant) {
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
    }

}
