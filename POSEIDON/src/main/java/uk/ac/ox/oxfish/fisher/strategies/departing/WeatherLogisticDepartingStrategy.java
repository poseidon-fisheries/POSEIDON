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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A logistic function decides whether to go out or not based on an environment harshness value that is itself
 * caused by windspeed, boat length and the likes
 * Created by carrknight on 9/11/15.
 */
public class WeatherLogisticDepartingStrategy extends LogisticDepartingStrategy {


    private static final long serialVersionUID = -1394247525382580346L;
    /**
     * how much windspeed affects "environment harshness"
     */
    private final double windspeedSensitivity;

    /**
     * how much boat length affects "environment harshness"
     */
    private final double boatLengthSensitivity;

    /**
     * with 0 length and 0 windspeed, what is the harshness level?
     */
    private final double harshnessIntercept;


    public WeatherLogisticDepartingStrategy(
        final double l, final double k, final double x0, final double windspeedSensitivity, final double boatLengthSensitivity,
        final double harshnessIntercept
    ) {
        super(l, k, x0);
        this.windspeedSensitivity = windspeedSensitivity;
        this.boatLengthSensitivity = boatLengthSensitivity;
        this.harshnessIntercept = harshnessIntercept;

    }

    /**
     * abstract method, returns whatever we need to plug in the logistic function
     *
     * @param fisher the fisher making the decision
     * @param model  the state
     */
    public double computeX(final Fisher fisher, final FishState model) {


        return fisher.getLocation().getWindSpeedInKph() * windspeedSensitivity +
            fisher.getBoat().getLength() * boatLengthSensitivity +
            harshnessIntercept;

    }
}
