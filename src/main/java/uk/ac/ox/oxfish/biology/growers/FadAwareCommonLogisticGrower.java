package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.ImmutableList;
import sim.engine.SimState;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.model.FishState;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.fisher.equipment.fads.Fad.biomassLostCounterName;

/**
 * This is a SchaeferLogisticGrower that:
 * - takes the biomass aggregated under the FADs into account as part of the total biomass.
 * - adds the biomass lost from FADs drifting out of the map or loosing fish over non-habitable
 * tiles back into the current biomass as part of the recruitment function.
 */
public class FadAwareCommonLogisticGrower extends SchaeferLogisticGrower {
    private final ImmutableList<BiomassLocalBiology> seaTileBiologies;
    private FishState model;

    FadAwareCommonLogisticGrower(
        double malthusianParameter,
        Species species,
        double distributionalWeight,
        ImmutableList<BiomassLocalBiology> seaTileBiologies
    ) {
        super(malthusianParameter, species, distributionalWeight);
        this.seaTileBiologies = seaTileBiologies;
        super.getBiologies().addAll(seaTileBiologies);
    }

    /**
     * Calls the normal recruitment function, but add the biomass lost by FADs to the total biomass recruited.
     */
    @Override protected double recruit(double current, double capacity, double malthusianParameter) {
        final double biomassLost = model.getFishers().stream()
            .mapToDouble(fisher -> fisher.getYearlyCounter().getColumn(biomassLostCounterName(species)))
            .sum();
        return biomassLost + super.recruit(current, capacity, malthusianParameter);
    }

    @Override public void step(SimState simState) {
        model = (FishState) simState;
        final List<BiomassLocalBiology> biologies = Stream.concat(
            seaTileBiologies.stream(),
            model.getFadMap().allFads().map(Fad::getBiology)
        ).collect(toList());
        grow(model, biologies, seaTileBiologies);
    }
}
