package uk.ac.ox.oxfish.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

@AutoService(FactorySupplier.class)
public class EverythingForbiddenFactorySupplier
    extends BasicFactorySupplier<EverythingForbidden> {
    public EverythingForbiddenFactorySupplier() {
        super(EverythingForbidden.class);
    }
}
