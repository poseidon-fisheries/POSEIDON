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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Simple departing strategy that forces agents to go out only as long as they haven't gone out more than x hours this year.
 * It isn't meant to be realistic but used when we want to fix effort at a level (for calibration purposes)
 * Created by carrknight on 3/29/17.
 */
public class MaxHoursPerYearDepartingStrategy implements DepartingStrategy {

    private final double maxHoursOut;


    public MaxHoursPerYearDepartingStrategy(double maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }

    @Override
    public void start(FishState model, Fisher fisher) {

    }

    @Override
    public void turnOff(Fisher fisher) {

    }

    /**
     * The fisher asks himself if he wants to leave the warm comfort of his bed.
     *
     * @param fisher
     * @param model
     * @param random
     * @return true if the fisherman wants to leave port.
     */
    @Override
    public boolean shouldFisherLeavePort(Fisher fisher, FishState model, MersenneTwisterFast random) {

        return fisher.getHoursAtSeaThisYear() < maxHoursOut;


    }

    @Override
    public int predictedDaysLeftFishingThisYear(Fisher fisher, FishState model, MersenneTwisterFast random) {
        return Math.min(
            365 - model.getDayOfTheYear(),
            (int) maxHoursOut / 24
        );
    }
}
