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

package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;

import java.util.function.Function;

/**
 * Created by carrknight on 8/11/16.
 */
public class ITQCostManager implements Cost{


    private final Function<Species,ITQOrderBook> orderBooks;


    public ITQCostManager(
            Function<Species,ITQOrderBook> orderBooks) {
        this.orderBooks = orderBooks;
    }

    /**
     * computes and return the cost
     *  @param fisher  agent that did the trip
     * @param model
     * @param record  the trip record
     * @param revenue revenue from catches   @return $ spent
     * @param durationInHours
     */
    @Override
    public double cost(Fisher fisher, FishState model, TripRecord record, double revenue, double durationInHours) {
        double total=0;
        for(Species species : model.getSpecies()) {
            ITQOrderBook market = orderBooks.apply(species);
            double biomass = record.getSoldCatch()[species.getIndex()];
            if (biomass > 0 && market != null) {
                double lastClosingPrice = market.getLastClosingPrice();

                if (Double.isFinite(lastClosingPrice)) {
                    //you could have sold those quotas!
                    total+= (lastClosingPrice * biomass);
                }

            }
        }
        return total;
    }

    @Override
    public double expectedAdditionalCosts(Fisher fisher, double additionalTripHours, double additionalEffortHours, double additionalKmTravelled) {
        return 0;
    }
}
