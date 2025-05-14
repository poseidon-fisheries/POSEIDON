package uk.ac.ox.poseidon.core.suppliers;

import lombok.RequiredArgsConstructor;

import java.util.function.DoubleSupplier;

@RequiredArgsConstructor
public class ConstantDoubleSupplier implements DoubleSupplier {

    private final double value;

    @Override
    public double getAsDouble() {
        return value;
    }
}
