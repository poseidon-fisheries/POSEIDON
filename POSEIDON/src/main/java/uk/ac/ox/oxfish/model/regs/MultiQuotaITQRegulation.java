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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;

import java.util.HashMap;

/**
 * Like multiquota but with a reference to ITQs used to compute opportunity costs
 * Created by carrknight on 4/20/16.
 */
public class MultiQuotaITQRegulation extends MultiQuotaRegulation {


    private static final long serialVersionUID = 2582856276395324315L;
    final private HashMap<Integer, ITQOrderBook> orderBooks;
    private ITQCostManager cost;

    public MultiQuotaITQRegulation(
        final double[] yearlyQuota, final FishState state, final HashMap<Integer, ITQOrderBook> orderBooks
    ) {
        super(yearlyQuota, state);
        this.orderBooks = orderBooks;
    }


    //compute opportunity costs!

    @Override
    public boolean isFishingStillAllowed() {
        for (final double remaining : quotaRemaining) {
            if (remaining < 0)
                return false;
        }
        return true;


        //this was a lot slower
//        return
//                Arrays.stream(quotaRemaining).allMatch(value -> value >= 0);


    }

    /**
     * add yourself as an opportunity cost!
     */
    @Override
    public void start(final FishState model, final Fisher fisher) {
        assert cost == null;
        cost = new ITQCostManager(species -> orderBooks.get(species.getIndex()));

        assert (!fisher.getOpportunityCosts().contains(cost));
        fisher.getOpportunityCosts().add(cost);

    }

    @Override
    public void turnOff(final Fisher fisher) {
        if (cost != null)
            fisher.getOpportunityCosts().remove(cost);
    }

    @Override
    public void reactToSale(
        final Species species, final Fisher seller, final double biomass, final double revenue, final FishState model, final int timeStep
    ) {
        super.reactToSale(species, seller, biomass, revenue, model, timeStep);

    }

    @VisibleForTesting
    public ITQOrderBook testOrderBook(final Species species) {
        return orderBooks.get(species.getIndex());
    }


}

