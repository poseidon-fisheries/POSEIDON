package uk.ac.ox.oxfish.regulations.quantities;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

@AutoService(FactorySupplier.class)
public class NumberOfActiveFadsFactorySupplier
    extends BasicFactorySupplier<NumberOfActiveFads> {
    public NumberOfActiveFadsFactorySupplier() {
        super(NumberOfActiveFads.class, "Number of active FADs");
    }
}
