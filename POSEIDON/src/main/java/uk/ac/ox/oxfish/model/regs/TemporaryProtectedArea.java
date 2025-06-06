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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 7/26/16.
 * <p>
 * To be deprecated. Use {@link TemporaryRegulation instead.}
 */
public class TemporaryProtectedArea implements Regulation {


    private final int firstDay;


    private final int finalDay;

    private final ProtectedAreasOnly activeDelegate = new ProtectedAreasOnly();

    private final Anarchy inactiveDelegate = new Anarchy();


    public TemporaryProtectedArea(final int firstDay, final int finalDay) {
        this.firstDay = firstDay;
        this.finalDay = finalDay;
        Preconditions.checkArgument(firstDay <= finalDay);

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
        final Fisher agent, final SeaTile tile, final FishState model, final int timeStep
    ) {
        return getCorrectDelegate(model).canFishHere(agent, tile, model, timeStep);
    }

    /**
     * if it's the protected area season then return that delegate, otherwise return the anarchy delegate
     *
     * @param state link to the model (to get current date)
     * @return appropriate delegate
     */
    private Regulation getCorrectDelegate(final FishState state) {
        if (state.getDayOfTheYear() >= firstDay && state.getDayOfTheYear() <= finalDay)
            return activeDelegate;
        else
            return inactiveDelegate;

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
        final Fisher agent, final Species species, final FishState model, final int timeStep
    ) {
        return getCorrectDelegate(model).maximumBiomassSellable(agent, species, model, timeStep);
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
    public boolean allowedAtSea(final Fisher fisher, final FishState model, final int timeStep) {
        return getCorrectDelegate(model).allowedAtSea(fisher, model, timeStep);
    }

    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new TemporaryProtectedArea(firstDay, finalDay);
    }

    /**
     * ignored
     */
    @Override
    public void start(final FishState model, final Fisher fisher) {
        activeDelegate.start(model, fisher);
        inactiveDelegate.start(model, fisher);
    }

    /**
     * ignored
     */
    @Override
    public void turnOff(final Fisher fisher) {
        activeDelegate.turnOff(fisher);
        inactiveDelegate.turnOff(fisher);
    }
}
