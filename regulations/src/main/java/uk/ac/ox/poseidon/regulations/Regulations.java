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

package uk.ac.ox.poseidon.regulations;

/**
 * A rule deciding whether a given {@link Action} is allowed. {@link ForbiddenIf} and
 * {@link PermittedIf} are the two base implementations; composition (e.g. all-of/any-of) is
 * expected to build on {@link uk.ac.ox.poseidon.core.predicates.logical} predicates over the same
 * action, rather than on {@code Regulations} composition directly.
 */
@FunctionalInterface
public interface Regulations<A extends Action<?>> {

    /** @return whether {@code action} is allowed under this rule */
    boolean isPermitted(A action);

    /** @return the negation of {@link #isPermitted} */
    default boolean isForbidden(final A action) {
        return !isPermitted(action);
    }

}
