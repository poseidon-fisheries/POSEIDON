package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.EverythingPermitted;

public class EverythingPermittedRegulationsFactory<C>
    implements AlgorithmFactory<Regulations<C>> {
    @Override
    public Regulations<C> apply(final FishState fishState) {
        return new EverythingPermitted<>();
    }
}
