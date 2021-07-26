package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;

/**
 * Now just a facade for FlexibleAbundanceMarket with PerBinPricingStrategy
 * For each bin (across subdivisions!) you are given its own price.
 *
 */
public class PerBinMarket extends FlexibleAbundanceMarket {


    public PerBinMarket(double[] pricePerBin) {
        super(new PerBinPricingStrategy(pricePerBin));

    }







}
