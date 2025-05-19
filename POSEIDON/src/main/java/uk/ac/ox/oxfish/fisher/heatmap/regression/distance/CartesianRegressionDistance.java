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

package uk.ac.ox.oxfish.fisher.heatmap.regression.distance;

/**
 * distance is (x-y)^2/bandwidth
 * Created by carrknight on 8/24/16.
 */
public class CartesianRegressionDistance implements RegressionDistance {


    private double bandwidth;


    public CartesianRegressionDistance(double bandwidth) {
        this.bandwidth = bandwidth;
    }

    @Override
    public double distance(double firstObservation, double secondObservation) {

        double distance = firstObservation - secondObservation;
        return distance * distance / bandwidth;
    }


    /**
     * Getter for property 'bandwidth'.
     *
     * @return Value for property 'bandwidth'.
     */
    public double getBandwidth() {
        return bandwidth;
    }

    /**
     * Setter for property 'bandwidth'.
     *
     * @param bandwidth Value to set for property 'bandwidth'.
     */
    public void setBandwidth(double bandwidth) {
        this.bandwidth = bandwidth;
    }


}
