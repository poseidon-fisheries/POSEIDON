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

package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Season;

/**
 * Seasonal dummy
 * Created by carrknight on 4/12/17.
 */
public class SeasonExtractor implements ObservationExtractor {


    private final Season correctSeason;

    public SeasonExtractor(Season correctSeason) {
        this.correctSeason = correctSeason;
    }

    @Override
    public double extract(
        SeaTile tile, double timeOfObservation, Fisher agent, FishState model
    ) {
        return Season.season(
            getDayOfTheYearFromHoursSinceStart(timeOfObservation, model)
        ).equals(correctSeason) ? 1d : 0d;
    }

    /**
     * @param hoursSinceStart
     * @param model
     * @return
     */
    public static int getDayOfTheYearFromHoursSinceStart(
        double hoursSinceStart,
        FishState model
    ) {


        return (getDaySinceStartFromHoursSinceStart(hoursSinceStart, model) % 365) + 1;

    }

    /**
     * this is useful for observations that store the observation time as hours since start but sometimes need to get back
     * what day of the year it actually was!
     *
     * @param hoursSinceStart
     * @return
     */
    public static int getDaySinceStartFromHoursSinceStart(
        double hoursSinceStart,
        FishState model
    ) {

        /*
        //get the steps
        double steps = hoursSinceStart / model.getHoursPerStep();
        //get days passed
        int days = (int) (steps / model.getStepsPerDay());
*/

//simplifies to this
        return (int) (hoursSinceStart / 24d);

    }
}
