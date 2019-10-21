/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package spikes;

public class JsonTestObject {


    final private String fisherId;

    final private double latitude;

    final private double longitude;


    public JsonTestObject(String fisherId, double latitude, double longitude) {
        this.fisherId = fisherId;
        this.latitude = latitude;
        this.longitude = longitude;
    }


    /**
     * Getter for property 'fisherId'.
     *
     * @return Value for property 'fisherId'.
     */
    public String getFisherId() {
        return fisherId;
    }

    /**
     * Getter for property 'latitude'.
     *
     * @return Value for property 'latitude'.
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter for property 'longitude'.
     *
     * @return Value for property 'longitude'.
     */
    public double getLongitude() {
        return longitude;
    }
}
