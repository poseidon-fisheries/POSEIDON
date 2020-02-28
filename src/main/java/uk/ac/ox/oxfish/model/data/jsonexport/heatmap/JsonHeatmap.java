/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.data.jsonexport.heatmap;

import uk.ac.ox.oxfish.model.data.jsonexport.JsonTimestep;

import java.util.LinkedList;
import java.util.List;

public class JsonHeatmap {


    private String name;

    private List<JsonTimestepGrid> timesteps = new LinkedList<>();

    private double[][] centroidsLatitude;

    private double[][] centroidsLongitude;

    public JsonHeatmap() {
    }


    /**
     * Getter for property 'name'.
     *
     * @return Value for property 'name'.
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for property 'name'.
     *
     * @param name Value to set for property 'name'.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for property 'timesteps'.
     *
     * @return Value for property 'timesteps'.
     */
    public List<JsonTimestepGrid> getTimesteps() {
        return timesteps;
    }

    /**
     * Setter for property 'timesteps'.
     *
     * @param timesteps Value to set for property 'timesteps'.
     */
    public void setTimesteps(List<JsonTimestepGrid> timesteps) {
        this.timesteps = timesteps;
    }

    /**
     * Getter for property 'centroidsLatitude'.
     *
     * @return Value for property 'centroidsLatitude'.
     */
    public double[][] getCentroidsLatitude() {
        return centroidsLatitude;
    }

    /**
     * Setter for property 'centroidsLatitude'.
     *
     * @param centroidsLatitude Value to set for property 'centroidsLatitude'.
     */
    public void setCentroidsLatitude(double[][] centroidsLatitude) {
        this.centroidsLatitude = centroidsLatitude;
    }

    /**
     * Getter for property 'centroidsLongitude'.
     *
     * @return Value for property 'centroidsLongitude'.
     */
    public double[][] getCentroidsLongitude() {
        return centroidsLongitude;
    }

    /**
     * Setter for property 'centroidsLongitude'.
     *
     * @param centroidsLongitude Value to set for property 'centroidsLongitude'.
     */
    public void setCentroidsLongitude(double[][] centroidsLongitude) {
        this.centroidsLongitude = centroidsLongitude;
    }
}
