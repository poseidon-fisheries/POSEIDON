package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.Arrays;
import java.util.Collection;

/**
 * A simple map Specie ---> Market
 * Created by carrknight on 5/3/15.
 */
public class MarketMap {

    private final Market[] marketList;

    public MarketMap(GlobalBiology biology)
    {
        marketList = new Market[biology.getSize()];
    }

    public void addMarket(Specie specie, Market market)
    {
        Preconditions.checkArgument( marketList[specie.getIndex()]==null);
        marketList[specie.getIndex()]=market;
    }

    public Market getMarket(Specie specie)
    {
        return marketList[specie.getIndex()];
    }


    public TradeInfo sellFish(Specie specie, double biomass, Fisher fisher,
                              Regulation regulation, FishState state)
    {
        return marketList[specie.getIndex()].sellFish(biomass,fisher,regulation,state,specie);

    }


    public Collection<Market> getMarkets()
    {
        return Arrays.asList(marketList);
    }



}
