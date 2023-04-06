package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public abstract class PerSpeciesCarryingCapacitiesFactory
    extends CarryingCapacitiesFactory
    implements AlgorithmFactory<DoubleParameter[]> {

    public PerSpeciesCarryingCapacitiesFactory() {
    }

    public PerSpeciesCarryingCapacitiesFactory(final DoubleParameter probabilityOfFadBeingDud) {
        super(probabilityOfFadBeingDud);
    }

    @Override
    public DoubleParameter[] apply(final FishState fishState) {
        final MersenneTwisterFast rng = fishState.getRandom();
        return fishState.getBiology()
            .getSpecies()
            .stream()
            .map(species ->
                makeCarryingCapacityParameter(
                    rng,
                    () -> makeSpeciesCarryingCapacityParameter(species, rng)
                )
            )
            .toArray(DoubleParameter[]::new);
    }

    abstract DoubleParameter makeSpeciesCarryingCapacityParameter(Species species, MersenneTwisterFast rng);
}
