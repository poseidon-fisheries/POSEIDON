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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A logistic function decides whether to go out or not based on an environment harshness value that is itself
 * caused by windspeed, boat length and the likes
 * Created by carrknight on 9/11/15.
 */
public class WeatherLogisticDepartingStrategy extends LogisticDepartingStrategy {


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
            double l, double k, double x0, double windspeedSensitivity, double boatLengthSensitivity,
            double harshnessIntercept) {
        super(l, k, x0);
        this.windspeedSensitivity = windspeedSensitivity;
        this.boatLengthSensitivity = boatLengthSensitivity;
        this.harshnessIntercept = harshnessIntercept;

    }

    /**
     * abstract method, returns whatever we need to plug in the logistic function
     *
     * @param fisher the fisher making the decision
     * @param model the state
     */
    public double computeX(Fisher fisher, FishState model){



        return fisher.getLocation().getWindSpeedInKph() * windspeedSensitivity +
                fisher.getBoat().getLength() * boatLengthSensitivity +
                harshnessIntercept;

    }
}
