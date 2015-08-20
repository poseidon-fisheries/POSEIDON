package uk.ac.ox.oxfish.model.market.itq;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * The promise to buy or sell at a specified price
 * Created by carrknight on 8/20/15.
 */
public class Quote implements Comparable<Quote>{



    final private double price;

    final private Fisher trader;


    public Quote(double price, Fisher trader) {
        Preconditions.checkArgument(price >=0);
        this.price = price;
        this.trader = trader;
    }

    /**
     * ranks by prices
     */
    @Override
    public int compareTo(Quote o) {
        return Double.compare(this.price,o.price);
    }

    public double getPrice() {
        return price;
    }

    public Fisher getTrader() {
        return trader;
    }
}
