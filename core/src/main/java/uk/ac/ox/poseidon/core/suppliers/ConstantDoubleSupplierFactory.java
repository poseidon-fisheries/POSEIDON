package uk.ac.ox.poseidon.core.suppliers;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConstantDoubleSupplierFactory extends GlobalScopeFactory<ConstantDoubleSupplier> {

    private double value;

    @Override
    protected ConstantDoubleSupplier newInstance(final Simulation simulation) {
        return new ConstantDoubleSupplier(value);
    }

}
