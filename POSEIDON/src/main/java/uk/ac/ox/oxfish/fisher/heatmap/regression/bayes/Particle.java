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

package uk.ac.ox.oxfish.fisher.heatmap.regression.bayes;

import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Created by carrknight on 7/29/16.
 */
public class Particle<V> {


    private V position;

    private double weight = 1;

    public Particle(V position) {
        this.position = position;
    }

    /**
     * Getter for property 'position'.
     *
     * @return Value for property 'position'.
     */
    public V getPosition() {
        return position;
    }

    public void setPosition(V position) {
        this.position = position;
    }

    /**
     * Getter for property 'weight'.
     *
     * @return Value for property 'weight'.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Setter for property 'weight'.
     *
     * @param weight Value to set for property 'weight'.
     */
    public void setWeight(double weight) {
        this.weight = FishStateUtilities.round5(weight);
    }
}
