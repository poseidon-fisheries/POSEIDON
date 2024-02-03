package uk.ac.ox.oxfish.regulations.quantities;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class YearlyCounterFactorySupplier
    extends BasicFactorySupplier<YearlyCounter> {
    public YearlyCounterFactorySupplier() {
        super(YearlyCounter.class);
    }
}
