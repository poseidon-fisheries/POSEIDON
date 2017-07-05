package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.Collection;

/**
 * A simple map Species ---> Market
 * Created by carrknight on 5/3/15.
 */
public class MarketMap {

    private final Market[] marketList;

    public MarketMap(GlobalBiology biology)
    {
        marketList = new Market[biology.getSize()];
    }

    public void addMarket(Species species, Market market)
    {
        Preconditions.checkArgument( marketList[species.getIndex()]==null);
        marketList[species.getIndex()]=market;
    }

    public Market getMarket(Species species)
    {
        return marketList[species.getIndex()];
    }


    public TradeInfo sellFish(
            Hold hold, Species species, Fisher fisher,
            Regulation regulation, FishState state)
    {
        return marketList[species.getIndex()].sellFish(hold, fisher, regulation, state, species);

    }


    public Collection<Market> getMarkets()
    {
        return Arrays.asList(marketList);
    }



}
