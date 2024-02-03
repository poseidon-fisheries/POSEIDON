package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class TemporalClosureExtensionAfterFactorySupplier
    extends BasicFactorySupplier<TemporalClosureExtensionAfterFactory> {
    public TemporalClosureExtensionAfterFactorySupplier() {
        super(TemporalClosureExtensionAfterFactory.class);
    }
}
