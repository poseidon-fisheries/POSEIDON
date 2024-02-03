package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class TemporalClosureFactorySupplier extends BasicFactorySupplier<TemporalClosure> {
    public TemporalClosureFactorySupplier() {
        super(TemporalClosure.class);
    }
}
