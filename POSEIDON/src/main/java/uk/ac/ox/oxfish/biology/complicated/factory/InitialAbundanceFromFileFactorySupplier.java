package uk.ac.ox.oxfish.biology.complicated.factory;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

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
