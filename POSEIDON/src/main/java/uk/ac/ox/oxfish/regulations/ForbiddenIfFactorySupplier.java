package uk.ac.ox.oxfish.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

@AutoService(FactorySupplier.class)
public class ForbiddenIfFactorySupplier extends BasicFactorySupplier<ForbiddenIf> {
    public ForbiddenIfFactorySupplier() {
        super(ForbiddenIf.class);
    }
}
