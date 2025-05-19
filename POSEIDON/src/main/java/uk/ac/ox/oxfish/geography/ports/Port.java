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

package uk.ac.ox.oxfish.geography.ports;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.market.Market;
import uk.ac.ox.oxfish.model.market.MarketMap;

import java.util.Collections;
import java.util.HashMap;
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
     * the markets available at this port
     */
    private final MarketMap defaultMarketMap;
    /**
     * the markets that ara available only to a selected few.
     */
    private final HashMap<Fisher, MarketMap> specializedMarketMaps = new HashMap<>();
    private final String name;
    /**
     * how much does gas cost per liter at this port
     */
    private double gasPricePerLiter;

    public Port(String portName, SeaTile location, MarketMap defaultMarketMap, double gasPricePerLiter) {
        this.name = portName;
        this.location = location;
        this.defaultMarketMap = defaultMarketMap;
        this.gasPricePerLiter = gasPricePerLiter;
        fishersHere = new HashSet<>();

        location.linkTileToPort(this);

    }

    /**
     * Tell the port the fisher is docking here. To be correct they must be on the same tile and the fisher must not
     * be already here
     *
     * @param fisher the fisher docking to port
     */
    public void dock(Fisher fisher) {

        Preconditions.checkArgument(
            fisher.getLocation().equals(location),
            "A fisher can't dock a port if they aren't on the same tile"
        );

        boolean wasHere = !fishersHere.add(fisher);
        if (wasHere)
            throw new IllegalStateException(fisher + " called dock() but is already here ");
        assert fishersHere.contains(fisher); //should be here now
    }

    /**
     * tell the port the fisher is departing
     *
     * @param fisher the fisher departing
     */
    public void depart(Fisher fisher) {
        boolean wasHere = fishersHere.remove(fisher);
        if (!wasHere)
            throw new IllegalStateException(fisher + " called undock() but wasn't in the list of docked fishers ");
        assert !fishersHere.contains(fisher); //shouldn't be here anymore

    }


    /**
     * get the marginal price of the market associated with this species
     *
     * @param species
     * @return
     */
    public double getMarginalPrice(Species species) {
        return defaultMarketMap.getMarket(species).getMarginalPrice();
    }


    /**
     * get the marginal price of the market associated with this species
     *
     * @param species
     * @return
     */
    public double getMarginalPrice(Species species, Fisher fisher) {
        return getMarketMap(fisher).getMarket(species).getMarginalPrice();
    }

    public MarketMap getMarketMap(Fisher fisher) {

        return specializedMarketMaps.getOrDefault(fisher, defaultMarketMap);

    }

    /**
     * returns an immutable view of the fishers listed here
     *
     * @return the set of fishers here
     */
    public Set<Fisher> getFishersHere() {
        return Collections.unmodifiableSet(fishersHere);
    }

    public boolean isDocked(Fisher fisher) {
        return fishersHere.contains(fisher);
    }

    public SeaTile getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Port " + getName() + " at " + location;
    }

    public String getName() {
        return name;
    }

    public MarketMap getDefaultMarketMap() {
        return defaultMarketMap;
    }

    public Market getMarket(Fisher fisher, Species species) {
        return getMarketMap(fisher).getMarket(species);
    }

    public double getGasPricePerLiter() {
        return gasPricePerLiter;
    }

    public void setGasPricePerLiter(double gasPricePerLiter) {
        this.gasPricePerLiter = gasPricePerLiter;
    }

    public void addSpecializedMarketMap(Fisher fisher, MarketMap specializedMarketMap) {
        specializedMarketMaps.put(fisher, specializedMarketMap);
    }

    public void removeSpecializedMarketMap(Fisher fisher) {
        specializedMarketMaps.remove(fisher);
    }


}
