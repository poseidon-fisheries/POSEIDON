/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (c) 2024-2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.poseidon.regulations.api;

import uk.ac.ox.poseidon.agents.api.Action;

import java.util.Collection;
import java.util.Collections;

import static uk.ac.ox.poseidon.regulations.api.Mode.FORBIDDEN;
import static uk.ac.ox.poseidon.regulations.api.Mode.MANDATORY;

public interface Regulations {

    default boolean isPermitted(final Action action) {
        // the action is permitted of the mode is either
        // PERMITTED or MANDATORY, but not FORBIDDEN
        return !isForbidden(action);
    }

    default boolean isForbidden(final Action action) {
        return mode(action) == FORBIDDEN;
    }

    Mode mode(Action action);

    default boolean isMandatory(final Action action) {
        return mode(action) == MANDATORY;
    }

    default Collection<Regulations> getSubRegulations() {
        return Collections.emptyList();
    }

}
