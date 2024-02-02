package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import com.google.auto.service.AutoService;
import uk.ac.ox.oxfish.utility.BasicFactorySupplier;
import uk.ac.ox.oxfish.utility.FactorySupplier;

@AutoService(FactorySupplier.class)
public class IndividualBetLimitsFactorySupplier
    extends BasicFactorySupplier<IndividualBetLimits> {
    public IndividualBetLimitsFactorySupplier() {
        super(IndividualBetLimits.class, "Individual BET limits");
    }
}
