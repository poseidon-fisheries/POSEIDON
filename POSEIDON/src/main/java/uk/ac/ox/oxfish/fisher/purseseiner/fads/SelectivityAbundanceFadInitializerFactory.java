package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceAggregatingFadInitializer;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.processSpeciesNameToDoubleParameterMap;

public class SelectivityAbundanceFadInitializerFactory
    extends AbundanceFadInitializerFactory
    implements AlgorithmFactory<FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad>> {

    private EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunction;

    public SelectivityAbundanceFadInitializerFactory() {
        super();
    }

    public SelectivityAbundanceFadInitializerFactory(
        final AlgorithmFactory<CarryingCapacitySupplier> carryingCapacitySupplier,
        final AbundanceFiltersFactory abundanceFiltersFactory,
        final DoubleParameter daysInWaterBeforeAttraction,
        final Map<String, DoubleParameter> catchabilities,
        final Map<String, DoubleParameter> fishReleaseProbabilities,
        final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunction
    ) {
        super(
            carryingCapacitySupplier,
            catchabilities,
            fishReleaseProbabilities,
            daysInWaterBeforeAttraction,
            abundanceFiltersFactory
        );
        this.environmentalPenaltyFunction = environmentalPenaltyFunction;
    }

    @SuppressWarnings("unused")
    public EnvironmentalPenaltyFunctionFactory getEnvironmentalPenaltyFunction() {
        return environmentalPenaltyFunction;
    }

    @SuppressWarnings("unused")
    public void setEnvironmentalPenaltyFunction(final EnvironmentalPenaltyFunctionFactory environmentalPenaltyFunction) {
        this.environmentalPenaltyFunction = environmentalPenaltyFunction;
    }

    protected FadInitializer<AbundanceLocalBiology, AbundanceAggregatingFad> makeFadInitializer(
        final FishState fishState
    ) {

        final GlobalBiology globalBiology = fishState.getBiology();

        final Function<SeaTile, Double> finalCatchabilityPenaltyFunction =
            environmentalPenaltyFunction.apply(fishState);

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
            Optional.ofNullable(environmentalPenaltyFunction)
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
                fishState,
                getAbundanceFilters().apply(fishState).get(FadSetAction.class)
            ),
            fishState::getStep,
            getCarryingCapacitySupplier().apply(fishState),
            processSpeciesNameToDoubleParameterMap(
                getFishReleaseProbabilities(),
                globalBiology,
                rng
            )
        );
    }

}
