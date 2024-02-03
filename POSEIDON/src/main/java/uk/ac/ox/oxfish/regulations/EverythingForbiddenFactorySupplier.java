package uk.ac.ox.oxfish.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class EverythingForbiddenFactorySupplier
    extends BasicFactorySupplier<EverythingForbidden> {
    public EverythingForbiddenFactorySupplier() {
        super(EverythingForbidden.class);
    }
}
