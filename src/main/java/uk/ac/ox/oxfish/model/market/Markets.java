package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Specie;

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

    public Collection<Market> asList()
    {
        return Arrays.asList(marketList);
    }
}
