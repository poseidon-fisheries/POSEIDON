package uk.ac.ox.poseidon.regulations.core.conditions;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class NotFactorySupplier
    extends BasicFactorySupplier<NotFactory> {
    public NotFactorySupplier() {
        super(NotFactory.class);
    }
}