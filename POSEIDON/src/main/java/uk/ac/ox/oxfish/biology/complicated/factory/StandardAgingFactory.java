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

package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.StandardAgingProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Created by carrknight on 7/7/17.
 */
public class StandardAgingFactory implements AlgorithmFactory<StandardAgingProcess> {


    /**
     * if this is false, last year fish dies off. Otherwise it accumulates in the last bin
     */
    boolean preserveLastAge = false;


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public StandardAgingProcess apply(FishState fishState) {
        return new StandardAgingProcess(preserveLastAge);
    }

    /**
     * Getter for property 'preserveLastAge'.
     *
     * @return Value for property 'preserveLastAge'.
     */
    public boolean isPreserveLastAge() {
        return preserveLastAge;
    }

    /**
     * Setter for property 'preserveLastAge'.
     *
     * @param preserveLastAge Value to set for property 'preserveLastAge'.
     */
    public void setPreserveLastAge(boolean preserveLastAge) {
        this.preserveLastAge = preserveLastAge;
    }
}
