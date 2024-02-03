package uk.ac.ox.oxfish.regulations.quantities;

import com.google.auto.service.AutoService;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.BasicFactorySupplier;

@AutoService(FactorySupplier.class)
public class LastYearlyFisherValueFactorySupplier
    extends BasicFactorySupplier<LastYearlyFisherValue> {
    public LastYearlyFisherValueFactorySupplier() {
        super(LastYearlyFisherValue.class);
    }
}
