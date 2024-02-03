package uk.ac.ox.poseidon.common.api;

import uk.ac.ox.poseidon.common.api.ComponentFactory;

import java.util.function.Supplier;

public interface FactorySupplier extends Supplier<ComponentFactory<?, ?>> {

    String getFactoryName();

    Class<? extends ComponentFactory<?, ?>> getFactoryClass();

}
