package uk.ac.ox.poseidon.common.api;

import java.util.function.Supplier;

public interface FactorySupplier extends Supplier<GenericComponentFactory<?, ?>> {

    String getFactoryName();

    Class<? extends GenericComponentFactory<?, ?>> getFactoryClass();

}
