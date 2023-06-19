package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class ConjunctiveRegulationsFactory implements AlgorithmFactory<ConjunctiveRegulations> {
    private Collection<AlgorithmFactory<? extends Regulation>> regulationFactories;

    public ConjunctiveRegulationsFactory() {
    }

    public ConjunctiveRegulationsFactory(
        final Collection<AlgorithmFactory<? extends Regulation>> regulationFactories
    ) {
        this.regulationFactories = regulationFactories;
    }

    @Override
    public ConjunctiveRegulations apply(final FishState fishState) {
        return new ConjunctiveRegulations(
            regulationFactories.stream().map(reg -> reg.apply(fishState)).collect(toImmutableList())
        );
    }

    public Collection<AlgorithmFactory<? extends Regulation>> getRegulationFactories() {
        return regulationFactories;
    }

    public void setRegulationFactories(final Collection<AlgorithmFactory<? extends Regulation>> regulationFactories) {
        this.regulationFactories = regulationFactories;
    }
}
