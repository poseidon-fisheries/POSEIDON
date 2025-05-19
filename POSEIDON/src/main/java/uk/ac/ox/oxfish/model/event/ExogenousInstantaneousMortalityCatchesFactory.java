/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.event;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;

public class ExogenousInstantaneousMortalityCatchesFactory implements
    AlgorithmFactory<ExogenousInstantaneousMortalityCatches> {


    private LinkedHashMap<String, Double> exogenousMortalities = new LinkedHashMap<>();

    private boolean isAbundanceBased = true;


    @Override
    public ExogenousInstantaneousMortalityCatches apply(final FishState fishState) {


        return new ExogenousInstantaneousMortalityCatches(
            "Exogenous catches of ",
            new LinkedHashMap<>(exogenousMortalities),
            isAbundanceBased
        );

    }


    public LinkedHashMap<String, Double> getExogenousMortalities() {
        return exogenousMortalities;
    }

    public void setExogenousMortalities(final LinkedHashMap<String, Double> exogenousMortalities) {
        this.exogenousMortalities = exogenousMortalities;
    }

    public boolean isAbundanceBased() {
        return isAbundanceBased;
    }

    public void setAbundanceBased(final boolean abundanceBased) {
        isAbundanceBased = abundanceBased;
    }
}
