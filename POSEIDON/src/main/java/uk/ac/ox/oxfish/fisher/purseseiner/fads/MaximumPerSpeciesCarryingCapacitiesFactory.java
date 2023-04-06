package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class MaximumPerSpeciesCarryingCapacitiesFactory extends PerSpeciesCarryingCapacitiesFactory {
    public MaximumPerSpeciesCarryingCapacitiesFactory() {
    }

    public MaximumPerSpeciesCarryingCapacitiesFactory(final DoubleParameter probabilityOfFadBeingDud) {
        super(probabilityOfFadBeingDud);
    }

    @Override
    DoubleParameter makeSpeciesCarryingCapacityParameter(final Species species, final MersenneTwisterFast rng) {
        return new FixedDoubleParameter(Double.MAX_VALUE);
    }
}
