package uk.ac.ox.oxfish.utility.bandit.factory;

import uk.ac.ox.oxfish.utility.bandit.BanditAlgorithm;
import uk.ac.ox.oxfish.utility.bandit.BanditAverage;
import java.util.function.Function;

/**
 * Created by carrknight on 11/11/16.
 */
public interface BanditSupplier extends Function<BanditAverage,BanditAlgorithm> {
}
