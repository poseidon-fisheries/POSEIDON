package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;

/**
 * Basic information about what is traded, how much of it and for what value
 * Created by carrknight on 5/3/15.
 */
public class TradeInfo {

    private final double biomassTraded;

    private final Species species;

    private final double moneyExchanged;

    public TradeInfo(double biomassTraded, Species species, double moneyExchanged) {
        this.biomassTraded = biomassTraded;
        this.species = species;
        this.moneyExchanged = moneyExchanged;
        Preconditions.checkArgument(biomassTraded>=0);
  //      Preconditions.checkArgument(moneyExchanged>=0); not true if it's a fine
    }

    public double getBiomassTraded() {
        return biomassTraded;
    }

    public Species getSpecies() {
        return species;
    }

    public double getMoneyExchanged() {
        return moneyExchanged;
    }
}
