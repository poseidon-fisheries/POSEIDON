package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class BiologyInitializerFactory<B extends LocalBiology>
    implements AlgorithmFactory<BiologyInitializer> {
    private AlgorithmFactory<Reallocator<B>> reallocator;

    public BiologyInitializerFactory(final AlgorithmFactory<Reallocator<B>> reallocator) {
        this.reallocator = reallocator;
    }

    public BiologyInitializerFactory() {
    }

    public AlgorithmFactory<Reallocator<B>> getReallocator() {
        return reallocator;
    }

    public void setReallocator(final AlgorithmFactory<Reallocator<B>> reallocator) {
        this.reallocator = reallocator;
    }
}
