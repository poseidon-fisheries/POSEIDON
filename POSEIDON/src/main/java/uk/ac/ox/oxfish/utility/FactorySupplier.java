package uk.ac.ox.oxfish.utility;

import java.util.function.Supplier;

public interface FactorySupplier extends Supplier<AlgorithmFactory<?>> {

    String getFactoryName();

    Class<? extends AlgorithmFactory<?>> getFactoryClass();

}
