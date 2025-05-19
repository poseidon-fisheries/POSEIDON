/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.market.itq;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * The promise to buy or sell at a specified price
 * Created by carrknight on 8/20/15.
 */
public class Quote implements Comparable<Quote> {


    private static int counter = 0;
    final private double price;
    final private Fisher trader;
    final private int splitter;


    public Quote(double price, Fisher trader) {
        Preconditions.checkArgument(price >= 0);
        this.price = price;
        this.trader = trader;
        splitter = counter++;

    }

    /**
     * ranks by prices
     */
    @Override
    public int compareTo(Quote o) {
        int compare = Double.compare(this.price, o.price);
        return compare == 0 ?
            Integer.compare(this.splitter, o.splitter) : compare;
    }

    public double getPrice() {
        return price;
    }

    public Fisher getTrader() {
        return trader;
    }

    @Override
    public String toString() {
        return price + "$";
    }
}
