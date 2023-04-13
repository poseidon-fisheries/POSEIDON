package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceAggregatingFadInitializer;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.CarryingCapacityInitializerFactory;
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

    private EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory;

    public SelectivityAbundanceFadInitializerFactory() {
        super();
    }

    public SelectivityAbundanceFadInitializerFactory(
        final CarryingCapacityInitializerFactory<PerSpeciesCarryingCapacity> carryingCapacityInitializerFactory,
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final DoubleParameter fadDudRate,
        final DoubleParameter daysInWaterBeforeAttraction,
        final DoubleParameter maximumDaysAttractions,
        final DoubleParameter fishReleaseProbabilityInPercent,
        final Map<String, DoubleParameter> catchabilities,
        final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory
    ) {
        super(
            carryingCapacityInitializerFactory,
            catchabilities,
            fadDudRate,
            daysInWaterBeforeAttraction,
            maximumDaysAttractions,
            fishReleaseProbabilityInPercent,
            abundanceFiltersFactory
        );
        this.environmentalPenaltyFunctionFactory = environmentalPenaltyFunctionFactory;
    }

    public SelectivityAbundanceFadInitializerFactory(
        final CarryingCapacityInitializerFactory<?> carryingCapacityInitializerFactory,
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final Map<String, DoubleParameter> catchabilities,
        final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory
    ) {
        super(carryingCapacityInitializerFactory, catchabilities, abundanceFiltersFactory);
        this.environmentalPenaltyFunctionFactory = environmentalPenaltyFunctionFactory;
    }

    public EnvironmentalPenaltyFunctionFactory getEnvironmentalPenaltyFunctionFactory() {
        return environmentalPenaltyFunctionFactory;
    }

    public void setEnvironmentalPenaltyFunctionFactory(final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunctionFactory) {
        this.environmentalPenaltyFunctionFactory = environmentalPenaltyFunctionFactory;
    }

    protected FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> makeFadInitializer(
        final FishState fishState
    ) {

        final GlobalBiology globalBiology = fishState.getBiology();

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

        final Function<Fad, double[]> catchabilitySupplier =
            Optional.ofNullable(environmentalPenaltyFunctionFactory)
                .map(factory -> factory.apply(fishState))
                .map(penalityFunction -> (Function<Fad, double[]>) fad -> {
                    final SeaTile fadLocation = fad.getLocation();
                    final double penaltyHere = finalCatchabilityPenaltyFunction.apply(fadLocation);
                    return (penaltyHere <= 0 || !Double.isFinite(penaltyHere))
                        ? new double[globalBiology.getSize()]
                        : Arrays.stream(catchabilityArray).map(c -> c * penaltyHere).toArray();
                })
                .orElse(fad -> catchabilityArray);

        return new AbundanceAggregatingFadInitializer(
            globalBiology,
            new CatchabilitySelectivityFishAttractor(
                catchabilitySupplier,
                (int) getDaysInWaterBeforeAttraction().applyAsDouble(rng),
                (int) getMaximumDaysAttractions().applyAsDouble(rng),
                fishState,
                getAbundanceFiltersFactory().apply(fishState).get(FadSetAction.class)
            ),
            getFishReleaseProbabilityInPercent().applyAsDouble(rng) / 100d,
            fishState::getStep,
            getCarryingCapacityInitializerFactory().apply(fishState)
        );
    }


}
