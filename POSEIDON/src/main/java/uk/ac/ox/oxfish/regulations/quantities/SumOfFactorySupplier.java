package uk.ac.ox.oxfish.regulations.quantities;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class SumOfFactorySupplier
    extends BasicFactorySupplier<SumOf> {
    public SumOfFactorySupplier() {
        super(SumOf.class);
    }
}
