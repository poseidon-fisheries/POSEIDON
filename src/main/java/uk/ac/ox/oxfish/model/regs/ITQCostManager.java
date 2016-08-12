package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;

import java.util.HashMap;
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
     *
     * @param fisher  agent that did the trip
     * @param model
     * @param record  the trip record
     * @param revenue revenue from catches   @return $ spent
     */
    @Override
    public double cost(Fisher fisher, FishState model, TripRecord record, double revenue) {
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
}
