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

package uk.ac.ox.oxfish.fisher.purseseiner.regulations;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class ActiveFadLimitsFactory implements ComponentFactory<Regulations> {

    // The map keys need to be strings to be accessible as Java beans, even though they
    // represent years. They will be converted to integers at the time of generating the
    // component. It's the user's responsibility to provide convertible values.
    private Map<String, ? extends Map<String, Integer>> limitsPerYearAndClass;

    @SuppressWarnings("unused")
    public ActiveFadLimitsFactory() {
    }

    @SuppressWarnings({"unused", "WeakerAccess"})
    public ActiveFadLimitsFactory(
        final Map<String, ? extends Map<String, Integer>> limitsPerYearAndClass
    ) {
        this.limitsPerYearAndClass = limitsPerYearAndClass;
    }

    @SuppressWarnings("unused")
    public Map<String, ? extends Map<String, Integer>> getLimitsPerYearAndClass() {
        return limitsPerYearAndClass;
    }

    @SuppressWarnings("unused")
    public void setLimitsPerYearAndClass(final Map<String, ? extends Map<String, Integer>> limitsPerYearAndClass) {
        this.limitsPerYearAndClass = limitsPerYearAndClass;
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        return new ActiveFadLimits(
            limitsPerYearAndClass.entrySet().stream().collect(toImmutableMap(
                entry -> Integer.valueOf(entry.getKey()),
                Map.Entry::getValue
            )),
            modelState
        );
    }
}
