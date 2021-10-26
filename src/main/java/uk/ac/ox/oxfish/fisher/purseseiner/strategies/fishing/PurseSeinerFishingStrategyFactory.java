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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;
import static uk.ac.ox.oxfish.model.scenario.EpoBiomassScenario.TARGET_YEAR;
import static uk.ac.ox.oxfish.model.scenario.EpoScenario.INPUT_PATH;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.collect.ImmutableMap;
import com.univocity.parsers.common.record.Record;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.DolphinSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.NonAssociatedSetAction;
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
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

abstract class PurseSeinerFishingStrategyFactory<B extends LocalBiology, F extends Fad<B, F>>
    implements AlgorithmFactory<PurseSeinerFishingStrategy<B, F>> {

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
    private double nonAssociatedSetGeneratorLogisticMidpoint = 100_000;
    private double nonAssociatedSetGeneratorLogisticSteepness = 1;
    private double dolphinSetGeneratorLogisticMidpoint = 100_000;
    private double dolphinSetGeneratorLogisticSteepness = 1;
    private double searchBonus = 0.1;
    private double nonAssociatedSetDetectionProbability = 0.1;
    private double dolphinSetDetectionProbability = 0.1;
    private double opportunisticFadSetDetectionProbability = 0.1;
    private double searchActionLogisticMidpoint = 0.1;
    private double searchActionLogisticSteepness = 1;
    private double searchActionDecayConstant = 1;
    private double fadDeploymentActionLogisticMidpoint = 0.1;
    private double fadDeploymentActionLogisticSteepness = 1;
    private double fadDeploymentActionDecayConstant = 1;
    private double fadSetActionLogisticSteepness = 1;
    private double fadSetActionLogisticMidpoint = 0.1;
    private double opportunisticFadSetActionLogisticSteepness = 1;
    private double opportunisticFadSetActionLogisticMidpoint = 0.1;
    private double nonAssociatedSetActionLogisticSteepness = 1;
    private double nonAssociatedSetActionLogisticMidpoint = 0.1;
    private double dolphinSetActionLogisticSteepness = 1;
    private double dolphinSetActionLogisticMidpoint = 0.1;
    private double movingThreshold = 0.1;

    PurseSeinerFishingStrategyFactory(
        final Class<B> biologyClass,
        final Class<F> fadClass
    ) {
        this.fadClass = fadClass;
        this.biologyClass = biologyClass;
    }

    public Path getAttractionWeightsFile() {
        return attractionWeightsFile;
    }

    public void setAttractionWeightsFile(final Path attractionWeightsFile) {
        this.attractionWeightsFile = attractionWeightsFile;
    }

    public CatchSamplersFactory<B> getCatchSamplersFactory() {
        return catchSamplersFactory;
    }

    public void setCatchSamplersFactory(final CatchSamplersFactory<B> catchSamplersFactory) {
        this.catchSamplersFactory = catchSamplersFactory;
    }

    public double getFadSetActionLogisticSteepness() {
        return fadSetActionLogisticSteepness;
    }

    public void setFadSetActionLogisticSteepness(final double fadSetActionLogisticSteepness) {
        this.fadSetActionLogisticSteepness = fadSetActionLogisticSteepness;
    }

    public double getFadSetActionLogisticMidpoint() {
        return fadSetActionLogisticMidpoint;
    }

    public void setFadSetActionLogisticMidpoint(final double fadSetActionLogisticMidpoint) {
        this.fadSetActionLogisticMidpoint = fadSetActionLogisticMidpoint;
    }

    public double getOpportunisticFadSetActionLogisticSteepness() {
        return opportunisticFadSetActionLogisticSteepness;
    }

    public void setOpportunisticFadSetActionLogisticSteepness(
        final double opportunisticFadSetActionLogisticSteepness
    ) {
        this.opportunisticFadSetActionLogisticSteepness =
            opportunisticFadSetActionLogisticSteepness;
    }

    public double getOpportunisticFadSetActionLogisticMidpoint() {
        return opportunisticFadSetActionLogisticMidpoint;
    }

    public void setOpportunisticFadSetActionLogisticMidpoint(
        final double opportunisticFadSetActionLogisticMidpoint
    ) {
        this.opportunisticFadSetActionLogisticMidpoint = opportunisticFadSetActionLogisticMidpoint;
    }

    public double getNonAssociatedSetActionLogisticSteepness() {
        return nonAssociatedSetActionLogisticSteepness;
    }

    public void setNonAssociatedSetActionLogisticSteepness(
        final double nonAssociatedSetActionLogisticSteepness
    ) {
        this.nonAssociatedSetActionLogisticSteepness = nonAssociatedSetActionLogisticSteepness;
    }

    public double getNonAssociatedSetActionLogisticMidpoint() {
        return nonAssociatedSetActionLogisticMidpoint;
    }

    public void setNonAssociatedSetActionLogisticMidpoint(
        final double nonAssociatedSetActionLogisticMidpoint
    ) {
        this.nonAssociatedSetActionLogisticMidpoint = nonAssociatedSetActionLogisticMidpoint;
    }

    public double getDolphinSetActionLogisticSteepness() {
        return dolphinSetActionLogisticSteepness;
    }

    public void setDolphinSetActionLogisticSteepness(
        final double dolphinSetActionLogisticSteepness
    ) {
        this.dolphinSetActionLogisticSteepness = dolphinSetActionLogisticSteepness;
    }

    public double getDolphinSetActionLogisticMidpoint() {
        return dolphinSetActionLogisticMidpoint;
    }

    public void setDolphinSetActionLogisticMidpoint(final double dolphinSetActionLogisticMidpoint) {
        this.dolphinSetActionLogisticMidpoint = dolphinSetActionLogisticMidpoint;
    }

    public double getMovingThreshold() {
        return movingThreshold;
    }

    public void setMovingThreshold(final double movingThreshold) {
        this.movingThreshold = movingThreshold;
    }

    public double getSearchActionLogisticMidpoint() {
        return searchActionLogisticMidpoint;
    }

    public void setSearchActionLogisticMidpoint(final double searchActionLogisticMidpoint) {
        this.searchActionLogisticMidpoint = searchActionLogisticMidpoint;
    }

    public double getSearchActionLogisticSteepness() {
        return searchActionLogisticSteepness;
    }

    public void setSearchActionLogisticSteepness(final double searchActionLogisticSteepness) {
        this.searchActionLogisticSteepness = searchActionLogisticSteepness;
    }

    public double getSearchActionDecayConstant() {
        return searchActionDecayConstant;
    }

    public void setSearchActionDecayConstant(final double searchActionDecayConstant) {
        this.searchActionDecayConstant = searchActionDecayConstant;
    }

    public double getFadDeploymentActionLogisticMidpoint() {
        return fadDeploymentActionLogisticMidpoint;
    }

    public void setFadDeploymentActionLogisticMidpoint(
        final double fadDeploymentActionLogisticMidpoint
    ) {
        this.fadDeploymentActionLogisticMidpoint = fadDeploymentActionLogisticMidpoint;
    }

    public double getFadDeploymentActionLogisticSteepness() {
        return fadDeploymentActionLogisticSteepness;
    }

    public void setFadDeploymentActionLogisticSteepness(
        final double fadDeploymentActionLogisticSteepness
    ) {
        this.fadDeploymentActionLogisticSteepness = fadDeploymentActionLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetDetectionProbability() {
        return opportunisticFadSetDetectionProbability;
    }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetDetectionProbability(
        final double opportunisticFadSetDetectionProbability
    ) {
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
        this.nonAssociatedSetDetectionProbability = nonAssociatedSetDetectionProbability;
    }

    public double getDolphinSetDetectionProbability() {
        return dolphinSetDetectionProbability;
    }

    public void setDolphinSetDetectionProbability(final double dolphinSetDetectionProbability) {
        this.dolphinSetDetectionProbability = dolphinSetDetectionProbability;
    }

    public double getSearchBonus() {
        return searchBonus;
    }

    public void setSearchBonus(final double searchBonus) {
        this.searchBonus = searchBonus;
    }

    public double getNonAssociatedSetGeneratorLogisticMidpoint() {
        return nonAssociatedSetGeneratorLogisticMidpoint;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetGeneratorLogisticMidpoint(
        final double nonAssociatedSetGeneratorLogisticMidpoint
    ) {
        this.nonAssociatedSetGeneratorLogisticMidpoint = nonAssociatedSetGeneratorLogisticMidpoint;
    }

    public double getNonAssociatedSetGeneratorLogisticSteepness() {
        return nonAssociatedSetGeneratorLogisticSteepness;
    }

    public void setNonAssociatedSetGeneratorLogisticSteepness(
        final double nonAssociatedSetGeneratorLogisticSteepness
    ) {
        this.nonAssociatedSetGeneratorLogisticSteepness =
            nonAssociatedSetGeneratorLogisticSteepness;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetGeneratorLogisticMidpoint() {
        return dolphinSetGeneratorLogisticMidpoint;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetGeneratorLogisticMidpoint(
        final double dolphinSetGeneratorLogisticMidpoint
    ) {
        this.dolphinSetGeneratorLogisticMidpoint = dolphinSetGeneratorLogisticMidpoint;
    }

    public double getDolphinSetGeneratorLogisticSteepness() {
        return dolphinSetGeneratorLogisticSteepness;
    }

    public void setDolphinSetGeneratorLogisticSteepness(
        final double dolphinSetGeneratorLogisticSteepness
    ) {
        this.dolphinSetGeneratorLogisticSteepness = dolphinSetGeneratorLogisticSteepness;
    }

    @Override
    public PurseSeinerFishingStrategy<B, F> apply(final FishState fishState) {
        checkNotNull(catchSamplersFactory);
        checkNotNull(attractionWeightsFile);
        return new PurseSeinerFishingStrategy<>(
            this::loadAttractionWeights,
            this::makeSetOpportunityDetector,
            makeActionValueFunctions(),
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

        //noinspection unchecked
        final SchoolSetOpportunityGenerator<B, NonAssociatedSetAction<B>>
            nonAssociatedSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<B, NonAssociatedSetAction<B>>(
                nonAssociatedSetGeneratorLogisticMidpoint,
                nonAssociatedSetGeneratorLogisticSteepness,
                setCompositionWeights.get(NonAssociatedSetAction.class),
                biologyClass,
                catchSamplersFactory.apply(fishState).get(NonAssociatedSetAction.class),
                NonAssociatedSetAction<B>::new,
                activeNonAssociatedSetOpportunitiesCache.get(fishState),
                durationSamplers.get(NonAssociatedSetAction.class)
            );

        final SchoolSetOpportunityGenerator<B, DolphinSetAction<B>>
            dolphinSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<>(
                dolphinSetGeneratorLogisticMidpoint,
                dolphinSetGeneratorLogisticSteepness,
                setCompositionWeights.get(DolphinSetAction.class),
                biologyClass,
                catchSamplersFactory.apply(fishState).get(DolphinSetAction.class),
                DolphinSetAction<B>::new,
                activeDolphinSetOpportunitiesCache.get(fishState),
                durationSamplers.get(DolphinSetAction.class)
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
    makeActionValueFunctions() {
        return new ImmutableMap.Builder<Class<? extends PurseSeinerAction>, DoubleUnaryOperator>()
            .put(
                SearchAction.class,
                new LogisticFunction(searchActionLogisticMidpoint, searchActionLogisticSteepness)
            )
            .put(
                FadDeploymentAction.class,
                new LogisticFunction(
                    fadDeploymentActionLogisticMidpoint,
                    fadDeploymentActionLogisticSteepness
                )
            )
            .put(
                NonAssociatedSetAction.class,
                new LogisticFunction(
                    nonAssociatedSetActionLogisticMidpoint,
                    nonAssociatedSetActionLogisticSteepness
                )
            )
            .put(
                DolphinSetAction.class,
                new LogisticFunction(
                    dolphinSetActionLogisticMidpoint,
                    dolphinSetActionLogisticSteepness
                )
            )
            .put(
                FadSetAction.class,
                new LogisticFunction(fadSetActionLogisticMidpoint, fadSetActionLogisticSteepness)
            )
            .put(
                OpportunisticFadSetAction.class,
                new LogisticFunction(
                    opportunisticFadSetActionLogisticMidpoint,
                    opportunisticFadSetActionLogisticSteepness
                )
            )
            .build();
    }

    public Path getSetCompositionWeightsPath() {
        return setCompositionWeightsPath;
    }

    public void setSetCompositionWeightsPath(final Path setCompositionWeightsPath) {
        this.setCompositionWeightsPath = setCompositionWeightsPath;
    }

    public double getFadDeploymentActionDecayConstant() {
        return fadDeploymentActionDecayConstant;
    }

    public void setFadDeploymentActionDecayConstant(final double fadDeploymentActionDecayConstant) {
        this.fadDeploymentActionDecayConstant = fadDeploymentActionDecayConstant;
    }

}
