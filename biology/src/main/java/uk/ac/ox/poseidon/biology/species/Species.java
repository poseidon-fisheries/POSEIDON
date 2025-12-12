/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology.species;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Species {

    private final String code;
    private final String name;
    private final String lifeStage;

    public Species(
        final String code,
        final String name
    ) {
        this(code, name, null);
    }

    /**
     * Returns true if a species "covers" another species, i.e., if it's the same according to its
     * code and life stage, or if it doesn't specify a life stage and the other does.
     * <p>
     * I'm adding this to handle the case where we have a price for a species as a whole, but
     * vessels catch that species at different life stages.
     * <p>
     * FIXME: this is needed, but a bit of a kludge and I'm not sure that this is the right approach
     *  in general. We probably need some kind of standard species ontology (with the possibility
     *  of encoding supra-species functional groups as well)
     */
    public boolean covers(final Species other) {
        return this.code.equals(other.code) &&
            (this.lifeStage == null || this.lifeStage.equals(other.lifeStage));
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(this.code);
        if (this.name != null && !this.code.isEmpty() && !this.code.equals(this.name)) {
            sb.append(" - ").append(this.name);
        }
        if (this.lifeStage != null && !this.lifeStage.isEmpty()) {
            sb.append(" (").append(this.lifeStage).append(")");
        }
        return sb.toString();
    }
}
