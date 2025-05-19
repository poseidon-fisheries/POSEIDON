/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;

public class BiomassDepletionGathererFactory implements AlgorithmFactory<BiomassDepletionGatherer> {


    /**
     * optionally you can get also msy collected
     */
    private HashMap<String, Double> msy = new HashMap<>();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public BiomassDepletionGatherer apply(FishState state) {
        return new BiomassDepletionGatherer(msy);
    }

    /**
     * Getter for property 'msy'.
     *
     * @return Value for property 'msy'.
     */
    public HashMap<String, Double> getMsy() {
        return msy;
    }

    /**
     * Setter for property 'msy'.
     *
     * @param msy Value to set for property 'msy'.
     */
    public void setMsy(HashMap<String, Double> msy) {
        this.msy = msy;
    }
}
