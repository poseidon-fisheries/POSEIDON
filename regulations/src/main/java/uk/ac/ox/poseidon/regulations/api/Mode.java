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

package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import static java.util.Objects.requireNonNull;

public enum Mode implements Regulations {

    PERMITTED, FORBIDDEN, MANDATORY;

    public static Mode and(final Mode a, final Mode b) {
        requireNonNull(a);
        requireNonNull(b);
        if (a == PERMITTED) {
            if (b == PERMITTED) {
                return PERMITTED;
            } else if (b == FORBIDDEN) {
                return FORBIDDEN;
            } else {  // b == MANDATORY
                return MANDATORY;
            }
        } else if (a == FORBIDDEN) {
            if (b == PERMITTED) {
                return FORBIDDEN;
            } else if (b == FORBIDDEN) {
                return FORBIDDEN;
            } else {  // b == MANDATORY
                throw new RuntimeException(a + " and " + b + " contradicted each other.");
            }
        } else {  // a == MANDATORY
            if (b == PERMITTED) {
                return MANDATORY;
            } else if (b == FORBIDDEN) {
                throw new RuntimeException(a + " and " + b + " contradicted each other.");
            } else {  // b == MANDATORY
                return MANDATORY;
            }
        }
    }

    static Mode not(final Mode a) {
        return requireNonNull(a) == PERMITTED ? FORBIDDEN : PERMITTED;
    }

    @Override
    public Mode mode(final Action action) {
        return this;
    }
}
