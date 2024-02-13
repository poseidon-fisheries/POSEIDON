package uk.ac.ox.poseidon.geography;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class DoubleGridByDateFromFileFactorySupplier
    extends BasicFactorySupplier<GridsByDateFromFileFactory> {
    public DoubleGridByDateFromFileFactorySupplier() {
        super(GridsByDateFromFileFactory.class);
    }
}
