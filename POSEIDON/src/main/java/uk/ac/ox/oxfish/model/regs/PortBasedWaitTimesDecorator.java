/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;

/**
 * simple regulation decorator. All it does is not allow fishers out until they stay enough time at port where the
 * number of hours waited depends on each port
 */
public class PortBasedWaitTimesDecorator implements Regulation {

    private final Regulation decorated;


    private final Map<String, Integer> hoursToWaitPerPort;


    public PortBasedWaitTimesDecorator(
        Regulation decorated, Map<String, Integer> hoursToWaitPerPort
    ) {
        this.decorated = decorated;
        this.hoursToWaitPerPort = hoursToWaitPerPort;
    }

    /**
     * can the agent fish at this location?
     *
     * @param agent the agent that wants to fish
     * @param tile  the tile the fisher is trying to fish on
     * @param model a link to the model
     * @return true if the fisher can fish
     */
    @Override
    public boolean canFishHere(
        Fisher agent, SeaTile tile, FishState model, int timeStep
    ) {
        return decorated.canFishHere(agent, tile, model, timeStep);
    }

    /**
     * how much of this species biomass is sellable. Zero means it is unsellable
     *
     * @param agent   the fisher selling its catch
     * @param species the species we are being asked about
     * @param model   a link to the model
     * @return a positive biomass if it sellable. Zero if you need to throw everything away
     */
    @Override
    public double maximumBiomassSellable(
        Fisher agent, Species species, FishState model, int timeStep
    ) {
        return decorated.maximumBiomassSellable(agent, species, model, timeStep);
    }

    /**
     * Can this fisher be at sea?
     *
     * @param fisher the  fisher
     * @param model  the model
     * @return true if it can be out. When it's false the fisher can't leave port and ought to go back to port if he is
     * at sea
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {

        if (fisher.isAtPortAndDocked() && fisher.getHoursAtPort() < hoursToWaitPerPort.get(fisher.getHomePort()
            .getName()))
            return false;

        return decorated.allowedAtSea(fisher, model, timeStep);
    }

    /**
     * tell the regulation object this much has been caught
     *
     * @param where             where the fishing occurred
     * @param who               who did the fishing
     * @param fishCaught        catch object
     * @param fishRetained
     * @param hoursSpentFishing how many hours were spent fishing
     */
    @Override
    public void reactToFishing(
        SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained, int hoursSpentFishing,
        FishState model, int timeStep
    ) {
        decorated.reactToFishing(where, who, fishCaught, fishRetained, hoursSpentFishing, model, timeStep);
    }

    /**
     * tell the regulation object this much of this species has been sold
     *
     * @param species the species of fish sold
     * @param seller  agent selling the fish
     * @param biomass how much biomass has been sold
     * @param revenue how much money was made off it
     */
    @Override
    public void reactToSale(
        Species species,
        Fisher seller,
        double biomass,
        double revenue,
        FishState model,
        int timeStep
    ) {
        decorated.reactToSale(species, seller, biomass, revenue, model, timeStep);
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new PortBasedWaitTimesDecorator(
            decorated.makeCopy(),
            hoursToWaitPerPort
        );
    }

    @Override
    public void start(FishState model, Fisher fisher) {

        decorated.start(model, fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {

        decorated.turnOff(fisher);
    }
}
