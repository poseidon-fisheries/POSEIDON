package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializer;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SelectivityAbundanceFadInitializerFactory
    extends AbundanceFadInitializerFactory {

    private GlobalCarryingCapacitiesFactory globalCarryingCapacitiesFactory;
    private EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory;

    public SelectivityAbundanceFadInitializerFactory() {
        super();
    }

    public SelectivityAbundanceFadInitializerFactory(
        final PerSpeciesCarryingCapacitiesFactory carryingCapacitiesFactory,
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final DoubleParameter fishValueCalculatorStandardDeviation,
        final DoubleParameter fadDudRate,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter maximumDaysAttractions,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final Map<String, DoubleParameter> catchabilities,
        final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory
    ) {
        super(
            carryingCapacitiesFactory,
            catchabilities,
            fishValueCalculatorStandardDeviation,
            fadDudRate,
            daysInWaterBeforeAttraction,
            maximumDaysAttractions,
            fishReleaseProbabilityInPercent,
            abundanceFiltersFactory
        );
        this.environmentalPenaltyFunctionFactory = environmentalPenaltyFunctionFactory;
    }

    public SelectivityAbundanceFadInitializerFactory(
        final PerSpeciesCarryingCapacitiesFactory carryingCapacitiesFactory,
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Map<String, DoubleParameter> catchabilities,
        final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory
    ) {
        super(carryingCapacitiesFactory, catchabilities, abundanceFiltersFactory);
        this.environmentalPenaltyFunctionFactory = environmentalPenaltyFunctionFactory;
    }

    public GlobalCarryingCapacitiesFactory getGlobalCarryingCapacitiesFactory() {
        return globalCarryingCapacitiesFactory;
    }

    public void setGlobalCarryingCapacitiesFactory(final GlobalCarryingCapacitiesFactory globalCarryingCapacitiesFactory) {
        this.globalCarryingCapacitiesFactory = globalCarryingCapacitiesFactory;
    }

    public EnvironmentalPenaltyFunctionFactory getEnvironmentalPenaltyFunctionFactory() {
        return environmentalPenaltyFunctionFactory;
    }

    public void setEnvironmentalPenaltyFunctionFactory(final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory) {
        this.environmentalPenaltyFunctionFactory = environmentalPenaltyFunctionFactory;
    }

    protected FadInitializer<AbundanceLocalBiology, AbundanceFad> makeFadInitializer(final FishState fishState) {

        final GlobalBiology globalBiology = fishState.getBiology();
        final DoubleParameter[] carryingCapacities =
            getCarryingCapacitiesFactory().apply(fishState);

        final Function<SeaTile, Double> finalCatchabilityPenaltyFunction =
            environmentalPenaltyFunctionFactory.apply(fishState);

        final MersenneTwisterFast rng = fishState.getRandom();

        final double[] catchabilityArray =
            globalBiology.getSpecies().stream()
                .mapToDouble(species ->
                    getCatchabilities()
                        .getOrDefault(species.getName(), new FixedDoubleParameter(0))
                        .applyAsDouble(rng)
                )
                .toArray();

        final Function<AbstractFad, double[]> catchabilitySupplier =
            Optional.ofNullable(environmentalPenaltyFunctionFactory)
                .map(factory -> factory.apply(fishState))
                .map(penalityFunction -> (Function<AbstractFad, double[]>) fad -> {
                    final SeaTile fadLocation = fad.getLocation();
                    final double penaltyHere = finalCatchabilityPenaltyFunction.apply(fadLocation);
                    return (penaltyHere <= 0 || !Double.isFinite(penaltyHere))
                        ? new double[globalBiology.getSize()]
                        : Arrays.stream(catchabilityArray).map(c -> c * penaltyHere).toArray();
                })
                .orElse(fad -> catchabilityArray);

        return new AbundanceFadInitializer(
            globalBiology,
            globalCarryingCapacitiesFactory.apply(fishState),
            new CatchabilitySelectivityFishAttractor(
                carryingCapacities,
                catchabilitySupplier,
                (int) getDaysInWaterBeforeAttraction().applyAsDouble(rng),
                (int) getMaximumDaysAttractions().applyAsDouble(rng),
                fishState,
                getAbundanceFiltersFactory().apply(fishState).get(FadSetAction.class)
            ),
            getFishReleaseProbabilityInPercent().applyAsDouble(rng) / 100d,
            fishState::getStep
        );
    }


}
