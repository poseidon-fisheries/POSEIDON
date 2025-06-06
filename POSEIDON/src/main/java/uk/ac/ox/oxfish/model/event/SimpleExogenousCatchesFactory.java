/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2018-2025, University of Oxford.
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


import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleExogenousCatchesFactory implements AlgorithmFactory<MixedExogenousCatches> {


    private HashMap<String, Number> yearlyBiomassToExtract = new HashMap<>();


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MixedExogenousCatches apply(final FishState fishState) {

        final LinkedHashMap<Species, Double> landings = new LinkedHashMap<>();
        for (final Map.Entry<String, Number> input : yearlyBiomassToExtract.entrySet()) {
            landings.put(
                fishState.getBiology().getSpeciesByCaseInsensitiveName(input.getKey()),
                input.getValue().doubleValue()
            );
        }


        return new MixedExogenousCatches(landings);

    }

    /**
     * Getter for property 'yearlyBiomassToExtract'.
     *
     * @return Value for property 'yearlyBiomassToExtract'.
     */
    public HashMap<String, Number> getYearlyBiomassToExtract() {
        return yearlyBiomassToExtract;
    }

    /**
     * Setter for property 'yearlyBiomassToExtract'.
     *
     * @param yearlyBiomassToExtract Value to set for property 'yearlyBiomassToExtract'.
     */
    public void setYearlyBiomassToExtract(final HashMap<String, Number> yearlyBiomassToExtract) {


        this.yearlyBiomassToExtract = yearlyBiomassToExtract;
    }
}
