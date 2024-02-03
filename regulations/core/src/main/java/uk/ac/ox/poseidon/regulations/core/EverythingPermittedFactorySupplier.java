package uk.ac.ox.poseidon.regulations.core;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class EverythingPermittedFactorySupplier
    extends BasicFactorySupplier<EverythingPermittedFactory> {
    public EverythingPermittedFactorySupplier() {
        super(EverythingPermittedFactory.class);
    }
}
