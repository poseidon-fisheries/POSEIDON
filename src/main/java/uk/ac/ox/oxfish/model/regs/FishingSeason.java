/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A regulation that allows fishing/selling/navigation only for the first x days of the year
 * Created by carrknight on 5/2/15.
 */
public class FishingSeason implements Regulation
{

    /**
     * if true MPAs are closed at all times
     */
    private boolean respectMPAs;

    /**
     * how many days a year is this open?
     */
    private int daysOpened;

    public FishingSeason(boolean respectMPAs, int daysOpened) {
        this.respectMPAs = respectMPAs;
        this.daysOpened = daysOpened;
    }


    private boolean seasonOpen(FishState state)
    {
        return state.getDayOfTheYear() <= daysOpened;
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
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model) {
        return   (!respectMPAs || !tile.isProtected());
    }

    /**
     * can sell anything as long as the fishing season is open
     */
    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model) {
        return Double.MAX_VALUE;
    }

    /**
     * they can as long as the fishing season is open
     */
    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model) {
        return seasonOpen(model);
    }

    /**
     * nothing
     */
    @Override
    public void reactToFishing(
            SeaTile where, Fisher who, Catch fishCaught, Catch fishRetained,
            int hoursSpentFishing) {

    }

    /**
     * nothing
     */
    @Override
    public void reactToSale(Species species, Fisher seller, double biomass, double revenue) {

    }

    public boolean isRespectMPAs() {
        return respectMPAs;
    }

    public int getDaysOpened() {
        return daysOpened;
    }


    /**
     * returns a copy of the regulation, used defensively
     *
     * @return
     */
    @Override
    public Regulation makeCopy() {
        return new FishingSeason(respectMPAs,daysOpened);
    }

    /**
     * Setter for property 'respectMPAs'.
     *
     * @param respectMPAs Value to set for property 'respectMPAs'.
     */
    public void setRespectMPAs(boolean respectMPAs) {
        this.respectMPAs = respectMPAs;
    }

    /**
     * ignored
     */
    @Override
    public void start(FishState model, Fisher fisher) {

    }

    /**
     * ignored
     */
    @Override
    public void turnOff(Fisher fisher) {

    }
}
