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

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;

import java.util.Arrays;
import java.util.HashMap;
import java.util.function.Function;

/**
 * Like multiquota but with a reference to ITQs used to compute opportunity costs
 * Created by carrknight on 4/20/16.
 */
public class MultiQuotaITQRegulation extends MultiQuotaRegulation  {


    final private HashMap<Integer,ITQOrderBook> orderBooks;

    public MultiQuotaITQRegulation(
            double[] yearlyQuota, FishState state, HashMap<Integer,ITQOrderBook> orderBooks) {
        super(yearlyQuota, state);
        this.orderBooks = orderBooks;
    }

    @Override
    public boolean isFishingStillAllowed() {
        for (double remaining : quotaRemaining) {
            if(remaining<0)
                return false;
        }
        return true;


        //this was a lot slower
//        return
//                Arrays.stream(quotaRemaining).allMatch(value -> value >= 0);


    }


    //compute opportunity costs!

    private ITQCostManager cost;

    /**
     * add yourself as an opportunity cost!
     * @param model
     * @param fisher
     */
    @Override
    public void start(FishState model, Fisher fisher) {
        assert cost == null;
        cost = new ITQCostManager(new Function<Species, ITQOrderBook>() {
            @Override
            public ITQOrderBook apply(Species species) {
                return orderBooks.get(species.getIndex());
            }
        });

        assert (!fisher.getOpportunityCosts().contains(cost));
        fisher.getOpportunityCosts().add(cost);

    }

    @Override
    public void turnOff(Fisher fisher) {
        if(cost!=null)
            fisher.getOpportunityCosts().remove(cost);
    }

    @Override
    public void reactToSale(
            Species species, Fisher seller, double biomass, double revenue) {
        super.reactToSale(species, seller, biomass, revenue);

    }

    @VisibleForTesting
    public ITQOrderBook testOrderBook(Species species)
    {
        return orderBooks.get(species.getIndex());
    }


}

