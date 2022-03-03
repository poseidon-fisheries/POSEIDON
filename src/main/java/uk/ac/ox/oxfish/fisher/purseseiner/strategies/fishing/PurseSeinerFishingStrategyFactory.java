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
import uk.ac.ox.oxfish.fisher.purseseiner.utils.CompressedExponentialFunction;
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
    private double nonAssociatedSetGeneratorCoefficient = 1E-6;
    private double nonAssociatedSetGeneratorExponent = 2;
    private double dolphinSetGeneratorCoefficient = 1E-6;
    private double dolphinSetGeneratorExponent = 2;
    private double searchBonus = 0.1;
    private double nonAssociatedSetDetectionProbability = 0.1;
    private double dolphinSetDetectionProbability = 0.1;
    private double opportunisticFadSetDetectionProbability = 0.1;
    private double searchActionCoefficient = 1E-6;
    private double searchActionExponent = 2;
    private double searchActionDecayConstant = 1;
    private double fadDeploymentActionCoefficient = 1E-6;
    private double fadDeploymentActionExponent = 2;
    private double fadDeploymentActionDecayConstant = 1;
    private double fadSetActionExponent = 2;
    private double fadSetActionCoefficient = 0.1;
    private double opportunisticFadSetActionExponent = 2;
    private double opportunisticFadSetActionCoefficient = 1E-6;
    private double nonAssociatedSetActionExponent = 2;
    private double nonAssociatedSetActionCoefficient = 1E-6;
    private double dolphinSetActionExponent = 2;
    private double dolphinSetActionCoefficient = 1E-6;
    private double movingThreshold = 0.1;

    PurseSeinerFishingStrategyFactory(
        final Class<B> biologyClass,
        final Class<F> fadClass
    ) {
        this.fadClass = fadClass;
        this.biologyClass = biologyClass;
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
    public double getFadSetActionExponent() {
        return fadSetActionExponent;
    }

    @SuppressWarnings("unused")
    public void setFadSetActionExponent(final double fadSetActionExponent) {
        this.fadSetActionExponent = fadSetActionExponent;
    }

    @SuppressWarnings("unused")
    public double getFadSetActionCoefficient() {
        return fadSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public void setFadSetActionCoefficient(final double fadSetActionCoefficient) {
        this.fadSetActionCoefficient = fadSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetActionExponent() {
        return opportunisticFadSetActionExponent;
    }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetActionExponent(
        final double opportunisticFadSetActionExponent
    ) {
        this.opportunisticFadSetActionExponent =
            opportunisticFadSetActionExponent;
    }

    @SuppressWarnings("unused")
    public double getOpportunisticFadSetActionCoefficient() {
        return opportunisticFadSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public void setOpportunisticFadSetActionCoefficient(
        final double opportunisticFadSetActionCoefficient
    ) {
        this.opportunisticFadSetActionCoefficient = opportunisticFadSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetActionExponent() {
        return nonAssociatedSetActionExponent;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetActionExponent(
        final double nonAssociatedSetActionExponent
    ) {
        this.nonAssociatedSetActionExponent = nonAssociatedSetActionExponent;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetActionCoefficient() {
        return nonAssociatedSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetActionCoefficient(
        final double nonAssociatedSetActionCoefficient
    ) {
        this.nonAssociatedSetActionCoefficient = nonAssociatedSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetActionExponent() {
        return dolphinSetActionExponent;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetActionExponent(
        final double dolphinSetActionExponent
    ) {
        this.dolphinSetActionExponent = dolphinSetActionExponent;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetActionCoefficient() {
        return dolphinSetActionCoefficient;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetActionCoefficient(final double dolphinSetActionCoefficient) {
        this.dolphinSetActionCoefficient = dolphinSetActionCoefficient;
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
    public double getSearchActionCoefficient() {
        return searchActionCoefficient;
    }

    @SuppressWarnings("unused")
    public void setSearchActionCoefficient(final double searchActionCoefficient) {
        this.searchActionCoefficient = searchActionCoefficient;
    }

    @SuppressWarnings("unused")
    public double getSearchActionExponent() {
        return searchActionExponent;
    }

    @SuppressWarnings("unused")
    public void setSearchActionExponent(final double searchActionExponent) {
        this.searchActionExponent = searchActionExponent;
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
    public double getFadDeploymentActionCoefficient() {
        return fadDeploymentActionCoefficient;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentActionCoefficient(
        final double fadDeploymentActionCoefficient
    ) {
        this.fadDeploymentActionCoefficient = fadDeploymentActionCoefficient;
    }

    @SuppressWarnings("unused")
    public double getFadDeploymentActionExponent() {
        return fadDeploymentActionExponent;
    }

    @SuppressWarnings("unused")
    public void setFadDeploymentActionExponent(
        final double fadDeploymentActionExponent
    ) {
        this.fadDeploymentActionExponent = fadDeploymentActionExponent;
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

    @SuppressWarnings("unused")
    public double getNonAssociatedSetGeneratorCoefficient() {
        return nonAssociatedSetGeneratorCoefficient;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetGeneratorCoefficient(
        final double nonAssociatedSetGeneratorCoefficient
    ) {
        this.nonAssociatedSetGeneratorCoefficient = nonAssociatedSetGeneratorCoefficient;
    }

    @SuppressWarnings("unused")
    public double getNonAssociatedSetGeneratorExponent() {
        return nonAssociatedSetGeneratorExponent;
    }

    @SuppressWarnings("unused")
    public void setNonAssociatedSetGeneratorExponent(
        final double nonAssociatedSetGeneratorExponent
    ) {
        this.nonAssociatedSetGeneratorExponent =
            nonAssociatedSetGeneratorExponent;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetGeneratorCoefficient() {
        return dolphinSetGeneratorCoefficient;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetGeneratorCoefficient(
        final double dolphinSetGeneratorCoefficient
    ) {
        this.dolphinSetGeneratorCoefficient = dolphinSetGeneratorCoefficient;
    }

    @SuppressWarnings("unused")
    public double getDolphinSetGeneratorExponent() {
        return dolphinSetGeneratorExponent;
    }

    @SuppressWarnings("unused")
    public void setDolphinSetGeneratorExponent(
        final double dolphinSetGeneratorExponent
    ) {
        this.dolphinSetGeneratorExponent = dolphinSetGeneratorExponent;
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

        final SchoolSetOpportunityGenerator<B, NonAssociatedSetAction<B>>
            nonAssociatedSetOpportunityGenerator =
            new SchoolSetOpportunityGenerator<>(
                nonAssociatedSetGeneratorCoefficient,
                nonAssociatedSetGeneratorExponent,
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
                dolphinSetGeneratorCoefficient,
                dolphinSetGeneratorExponent,
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
                new CompressedExponentialFunction(
                    searchActionCoefficient,
                    searchActionExponent
                )
            )
            .put(
                FadDeploymentAction.class,
                new CompressedExponentialFunction(
                    fadDeploymentActionCoefficient,
                    fadDeploymentActionExponent
                )
            )
            .put(
                NonAssociatedSetAction.class,
                new CompressedExponentialFunction(
                    nonAssociatedSetActionCoefficient,
                    nonAssociatedSetActionExponent
                )
            )
            .put(
                DolphinSetAction.class,
                new CompressedExponentialFunction(
                    dolphinSetActionCoefficient,
                    dolphinSetActionExponent
                )
            )
            .put(
                FadSetAction.class,
                new CompressedExponentialFunction(
                    fadSetActionCoefficient,
                    fadSetActionExponent
                )
            )
            .put(
                OpportunisticFadSetAction.class,
                new CompressedExponentialFunction(
                    opportunisticFadSetActionCoefficient,
                    opportunisticFadSetActionExponent
                )
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
