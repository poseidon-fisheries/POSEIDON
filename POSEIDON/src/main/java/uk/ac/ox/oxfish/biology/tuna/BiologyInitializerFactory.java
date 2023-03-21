package uk.ac.ox.oxfish.biology.tuna;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public abstract class BiologyInitializerFactory<K, B extends LocalBiology>
    implements AlgorithmFactory<BiologyInitializer> {
    private Reallocator<K, B> reallocator;

    public Reallocator<K, B> getReallocator() {
        return reallocator;
    }

    public void setReallocator(final Reallocator<K, B> reallocator) {
        this.reallocator = reallocator;
    }
}
