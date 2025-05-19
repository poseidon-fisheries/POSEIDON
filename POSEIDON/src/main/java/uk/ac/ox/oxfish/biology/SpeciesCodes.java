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

package uk.ac.ox.oxfish.biology;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpeciesCodes {

    private final BiMap<String, String> bimap;

    /**
     * @param bimap A map from species code to species name.
     */
    public SpeciesCodes(final Map<String, String> bimap) {
        this.bimap = ImmutableBiMap.copyOf(bimap);
    }

    public Set<String> getSpeciesNames() {
        return bimap.values();
    }

    @SuppressWarnings("unused")
    public Set<String> getSpeciesCodes() {
        return bimap.keySet();
    }

    public Species getSpeciesFromCode(final GlobalBiology globalBiology, final String speciesCode) {
        return globalBiology.getSpeciesByCaseInsensitiveName(getSpeciesName(speciesCode));
    }

    public String getSpeciesName(final String speciesCode) {
        return Optional.ofNullable(bimap.get(speciesCode))
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown species name for code " + speciesCode));
    }

    public String getSpeciesCode(final String speciesName) {
        return Optional.ofNullable(bimap.inverse().get(speciesName))
            .orElseThrow(() -> new IllegalArgumentException(
                "Unknown species code for name " + speciesName));

    }

}
