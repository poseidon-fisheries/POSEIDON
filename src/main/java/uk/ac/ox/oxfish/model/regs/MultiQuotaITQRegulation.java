package uk.ac.ox.oxfish.model.regs;

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


    final private HashMap<Integer,ITQOrderBook> orderBooks;

    public MultiQuotaITQRegulation(
            double[] yearlyQuota, FishState state, HashMap<Integer,ITQOrderBook> orderBooks) {
        super(yearlyQuota, state);
        this.orderBooks = orderBooks;
    }


    //compute opportunity costs!



    @Override
    public void reactToSale(
            Species species, Fisher seller, double biomass, double revenue) {
        super.reactToSale(species, seller, biomass, revenue);
        ITQOrderBook market = orderBooks.get(species.getIndex());
        if(biomass>0 && market != null )
        {
            double lastClosingPrice = market.getLastClosingPrice();

            if(Double.isFinite(lastClosingPrice))
            {
                //you could have sold those quotas!
                seller.recordOpportunityCosts(lastClosingPrice * biomass);
            }

        }
    }
}

