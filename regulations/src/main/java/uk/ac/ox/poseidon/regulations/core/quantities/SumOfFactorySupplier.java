package uk.ac.ox.poseidon.regulations.core.quantities;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class SumOfFactorySupplier
    extends BasicFactorySupplier<SumOfFactory> {
    public SumOfFactorySupplier() {
        super(SumOfFactory.class);
    }
}
