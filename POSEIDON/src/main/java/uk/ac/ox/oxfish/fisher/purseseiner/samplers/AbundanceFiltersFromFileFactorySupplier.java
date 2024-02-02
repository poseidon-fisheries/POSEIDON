package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

@AutoService(FactorySupplier.class)
public class AbundanceFiltersFromFileFactorySupplier
    extends BasicFactorySupplier<AbundanceFiltersFromFileFactory> {
    public AbundanceFiltersFromFileFactorySupplier() {
        super(AbundanceFiltersFromFileFactory.class);
    }
}
