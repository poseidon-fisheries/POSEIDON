package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Specie;

/**
 * Basic information about what is traded, how much of it and for what value
 * Created by carrknight on 5/3/15.
 */
public class TradeInfo {

    private final double biomassTraded;

    private final Specie specie;

    private final double moneyExchanged;

    public TradeInfo(double biomassTraded, Specie specie, double moneyExchanged) {
        this.biomassTraded = biomassTraded;
        this.specie = specie;
        this.moneyExchanged = moneyExchanged;
        Preconditions.checkArgument(biomassTraded>=0);
        Preconditions.checkArgument(moneyExchanged>=0);
    }

    public double getBiomassTraded() {
        return biomassTraded;
    }

    public Specie getSpecie() {
        return specie;
    }

    public double getMoneyExchanged() {
        return moneyExchanged;
    }
}
