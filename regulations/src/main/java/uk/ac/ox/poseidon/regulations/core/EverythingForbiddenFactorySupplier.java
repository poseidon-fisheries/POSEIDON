package uk.ac.ox.poseidon.regulations.core;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class EverythingForbiddenFactorySupplier
    extends BasicFactorySupplier<EverythingForbiddenFactory> {
    public EverythingForbiddenFactorySupplier() {
        super(EverythingForbiddenFactory.class);
    }
}
