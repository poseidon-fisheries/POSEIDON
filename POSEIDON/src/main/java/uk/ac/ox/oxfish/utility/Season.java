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

package uk.ac.ox.oxfish.utility;

import uk.ac.ox.oxfish.model.FishState;

/**
 * Just a marker for season
 * Created by carrknight on 12/2/16.
 */
public enum Season {

    SPRING,

    SUMMER,

    FALL,

    WINTER;

    /**
     * turns day of the year into season, approximately
     * @param dayOfTheYear the day of the year
     * @return
     */
    public static Season season(int dayOfTheYear)
    {

        int month = getMonth(dayOfTheYear);
        int day = (int) (dayOfTheYear % 30.42);


        //stolen from here:
        // http://stackoverflow.com/a/9501252/975904
        if ((month == 3 & day >= 21) | (month == 4) | (month == 5) | (month == 6 & day < 21)) {
            return SPRING;
        } else if ((month == 6 & day >= 21) | (month == 7) | (month == 8) | (month == 9 & day < 21)) {
            return SUMMER;
        } else if ((month == 9 & day >= 21) | (month == 10) | (month == 11) | (month == 12 & day < 21)) {
            return FALL;
        } else {
            return WINTER;
        }

    }

    public static int getMonth(int dayOfTheYear) {
        return (int)(dayOfTheYear / 30.42) + 1;
    }

    public static Season season(FishState model)
    {

        return season(model.getDayOfTheYear());

    }

}
