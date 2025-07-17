package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import org.apache.commons.math3.util.DoubleArray;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.AbundanceFiltersFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.AbundanceAggregatingFadInitializer;
import uk.ac.ox.oxfish.geography.fads.AbundanceFadInitializerFactory;
import uk.ac.ox.oxfish.geography.fads.FadInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalMapFactory;
import uk.ac.ox.oxfish.model.plugins.EnvironmentalPenaltyFunctionFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

import static java.lang.Math.*;
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

                    final Double[] penaltyHere = new Double[globalBiology.getSize()];
                    for(int i=0; i<penaltyHere.length; i++){

                        Collection<EnvironmentalMapFactory> maps =
                            environmentalPenaltyFunction.getEnvironmentalMapFactories().values();
                        penaltyHere[i] = 1.0;
                        for(EnvironmentalMapFactory map: maps){
                            String mapName = map.getMapVariableName();
                            double margin = map.getMargin().applyAsDouble(fishState.getRandom());
                            double penalty = map.getPenalty().applyAsDouble(fishState.getRandom());
                            double target = map.getTarget(i).applyAsDouble(fishState.getRandom());
                            double valueHere =
                                fishState.getMap()
                                    .getAdditionalMaps()
                                    .get(mapName)
                                    .get()
                                    .get(fadLocation.getGridX(), fadLocation.getGridY());
                            double valueDifference = abs(valueHere - target) - margin;
                            penaltyHere[i] *= (valueDifference > 0) ? 1 / pow(1 + (-valueDifference * log(1 - penalty)), 4) : 1;

                        }
//                        for(int j=0; j<maps.size(); j++){
//                            EnvironmentalMapFactory map = ;
//                        }
                    }

                    //finalCatchabilityPenaltyFunction.apply(fadLocation);
                    if(penaltyHere[0] <= 0 || !Double.isFinite(penaltyHere[0])){
                        return new double[globalBiology.getSize()];
                    } else {
                        double[] multiplierHere = new double[catchabilityArray.length];
                        for(int i=0; i<multiplierHere.length; i++){
                            multiplierHere[i] = catchabilityArray[i]*penaltyHere[i];
                        }
                        return multiplierHere;
                    }
//                    return (penaltyHere[0] <= 0 || !Double.isFinite(penaltyHere[0]))
//                        ? new double[globalBiology.getSize()]
//                        : IntStream.range(0,catchabilityArray.length).map(i -> catchabilityArray[i] * penaltyHere[i]).toArray();
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
