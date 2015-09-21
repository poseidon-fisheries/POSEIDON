package uk.ac.ox.oxfish.fisher;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Specie;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A location fishermen come and go from
 * Created by carrknight on 4/2/15.
 */
public class Port {

    /**
     * all the fishers anchored here
     */
    private final HashSet<Fisher> fishersHere;

    /**
     * the location of the port
     */
    private final SeaTile location;


    /**
     * how much does gas cost per liter at this port
     */
    private double gasPricePerLiter;


    /**
     *  the markets available at this port
     */
    private final MarketMap marketMap;

    public Port(SeaTile location, MarketMap marketMap, double gasPricePerLiter)
    {
        this.location = location;
        this.marketMap = marketMap;
        this.gasPricePerLiter = gasPricePerLiter;
        fishersHere = new HashSet<>();

        location.linkTileToPort(this);

    }

    /**
     * Tell the port the fisher is docking here. To be correct they must be on the same tile and the fisher must not
     * be already here
     * @param fisher the fisher docking to port
     */
    public void dock(Fisher fisher)
    {

        Preconditions.checkArgument(fisher.getLocation().equals(location),
                "A fisher can't dock a port if they aren't on the same tile");

        boolean wasHere = !fishersHere.add(fisher);
        if(wasHere)
            throw new IllegalStateException(fisher +" called dock() but is already here ");
        assert fishersHere.contains(fisher); //should be here now
    }

    /**
     * tell the port the fisher is departing
     * @param fisher the fisher departing
     */
    public void depart(Fisher fisher)
    {
        boolean wasHere =fishersHere.remove(fisher);
        if(!wasHere)
            throw new IllegalStateException(fisher +" called undock() but wasn't in the list of docked fishers ");
        assert !fishersHere.contains(fisher); //shouldn't be here anymore

    }


    /**
     * returns an immutable view of the fishers listed here
     * @return the set of fishers here
     */
    public Set<Fisher> getFishersHere() {
        return Collections.unmodifiableSet(fishersHere);
    }


    public boolean isDocked(Fisher fisher)
    {
        return fishersHere.contains(fisher);
    }


    public SeaTile getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Port at " +location;
    }

    public MarketMap getMarketMap() {
        return marketMap;
    }

    public Market getMarket(Specie specie) {
        return marketMap.getMarket(specie);
    }

    public double getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(double gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }
}
