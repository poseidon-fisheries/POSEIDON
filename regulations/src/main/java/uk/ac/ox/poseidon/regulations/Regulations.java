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

import java.util.function.Predicate;

/**
 * A rule judging whether an {@link Action} is legal: a {@link Predicate} over actions that holds
 * for the actions it permits. {@link #isPermitted} and {@link #isForbidden} say the same as
 * {@link #test}, in the words of the rule.
 * <p>
 * A rule is built from descriptive predicates over the action (where, when, by whom) with
 * {@link ForbiddenIf} or {@link PermittedIf}, or is a component of its own, such as a catch
 * quota. Rules combine by conjunction, {@link AllOfRegulations}: an action is legal when every
 * rule permits it. Duties that are not a judgement of legality (quota accounting, fines, quota
 * markets, what to discard) belong to other components, which rules may read.
 */
@FunctionalInterface
public interface Regulations<A extends Action<?>> extends Predicate<A> {

    /** @return whether {@code action} is allowed under this rule */
    boolean isPermitted(A action);

    /** @return the negation of {@link #isPermitted} */
    default boolean isForbidden(final A action) {
        return !isPermitted(action);
    }

    /** @return whether {@code action} is allowed under this rule, as {@link #isPermitted} */
    @Override
    default boolean test(final A action) {
        return isPermitted(action);
    }

}
