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

package uk.ac.ox.oxfish.fisher.strategies.weather;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FisherStartable;

/**
 * The strategy used by the fisher to fish
 * Created by carrknight on 9/7/15.
 */
public interface WeatherEmergencyStrategy extends FisherStartable{


    /**
     * return what the new value of the weather emergency flag ought to be: this overrides other behaviors of the fisher
     * when true and make it go home (or stay there)
     * @param currentEmergencyFlag is the weather flag on or off currently?
     * @param fisher the fisher making the decision
     * @param location where the fisher is currently
     * @return true if the fisher needs to stop what he's doing and go home
     */
    boolean updateWeatherEmergencyFlag(
            boolean currentEmergencyFlag, Fisher fisher,
            SeaTile location);



}
