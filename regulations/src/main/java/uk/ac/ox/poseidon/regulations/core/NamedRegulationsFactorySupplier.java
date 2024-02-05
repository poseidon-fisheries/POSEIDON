package uk.ac.ox.poseidon.regulations.core;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class NamedRegulationsFactorySupplier extends BasicFactorySupplier<NamedRegulationsFactory> {
    public NamedRegulationsFactorySupplier() {
        super(NamedRegulationsFactory.class);
    }
}
