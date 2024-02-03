package uk.ac.ox.oxfish.biology.complicated.factory;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class InitialAbundanceFromFileFactorySupplier
    extends BasicFactorySupplier<InitialAbundanceFromFileFactory> {

    public InitialAbundanceFromFileFactorySupplier() {
        super(
            InitialAbundanceFromFileFactory.class,
            "Abundance From File"
        );
    }

}
