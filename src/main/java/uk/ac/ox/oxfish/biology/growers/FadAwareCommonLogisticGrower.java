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

public class FadAwareCommonLogisticGrower extends SchaeferLogisticGrower {
    private final ImmutableList<BiomassLocalBiology> seaTileBiologies;

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

    @Override public void step(SimState simState) {
        final FishState model = (FishState) simState;
        final List<BiomassLocalBiology> biologies = Stream.concat(
            seaTileBiologies.stream(),
            model.getFadMap().allFads().map(Fad::getBiology)
        ).collect(toList());
        grow(model, biologies,seaTileBiologies);
    }
}
