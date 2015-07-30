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
public class Markets {

    private final Market[] marketList;

    public Markets(GlobalBiology biology)
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
        return marketList[specie.getIndex()].sellFish(biomass,fisher,regulation,state);

    }


    public double estimateTotalValue(Catch fishingCatch, Fisher fisher, Regulation regulation)
    {
        double total = 0;
        for(int i=0; i<marketList.length; i++)
        {
            double specieCatch =  fishingCatch.getPoundsCaught(i);
            if(specieCatch > 0)
                total += specieCatch * marketList[i].expectedRevenueFromSellingThisBiomass(specieCatch,fisher,regulation);
        }
        assert  total>=0;
        return total;
    }

    public Collection<Market> getMarkets()
    {
        return Arrays.asList(marketList);
    }


}
