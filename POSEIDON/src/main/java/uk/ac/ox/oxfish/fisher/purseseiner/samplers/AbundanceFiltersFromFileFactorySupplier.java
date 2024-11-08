package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class AbundanceFiltersFromFileFactorySupplier
    extends BasicFactorySupplier<AbundanceFiltersFromFileFactory> {
    public AbundanceFiltersFromFileFactorySupplier() {
        super(AbundanceFiltersFromFileFactory.class);
    }
}
