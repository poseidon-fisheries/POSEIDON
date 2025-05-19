/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.market;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.Startable;

/**
 * pricing algorithm for an abundance based market.
 * Basically the "strategy" pattern so we can swap in and out different pricing systems (or just change
 * prices) without touching the market object itself.
 */
public interface PricingStrategy extends Startable {


    /**
     * get the price commander per unit of fish sold for this fish at this bin (as in abundance bin)
     *
     * @param speciesBeingSold species being sold
     * @param seller           who is selling
     * @param biologicalBin    the abundance "bin" i.e. either the length or the age (depending on the model)
     * @param quantitySold     how much is being sold
     * @return price PER UNIT (not scaled to quantity sold!)
     */
    public double getPricePerKg(
        Species speciesBeingSold,
        Fisher seller,
        int biologicalBin,
        double quantitySold
    );


    public void reactToSale(TradeInfo info);

    /**
     * a guessed price of what the next kg sold will be; this is a bad interface we
     * are inheriting from AbstractMarket
     */
    public double getMarginalPrice();


}
