/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.market;

import com.google.common.base.MoreObjects;
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


    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("biomassTraded", biomassTraded)
                .add("species", species)
                .add("moneyExchanged", moneyExchanged)
                .toString();
    }
}
