/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.Predicate;

/**
 * Factories for the two base {@link Regulations} rules, {@link ForbiddenIf} and
 * {@link PermittedIf}, each built from a predicate over the action.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link ForbiddenIf}
     * @see ForbiddenIfFactory
     */
    public static <S extends Scope, A extends Action<?>> ForbiddenIfFactory<S, A> forbiddenIf(
        final Factory<? super S, ? extends Predicate<? super A>> actionPredicate
    ) {
        return new ForbiddenIfFactory<S, A>(actionPredicate);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link PermittedIf}
     * @see PermittedIfFactory
     */
    public static <S extends Scope, A extends Action<?>> PermittedIfFactory<S, A> permittedIf(
        final Factory<? super S, ? extends Predicate<? super A>> actionPredicate
    ) {
        return new PermittedIfFactory<S, A>(actionPredicate);
    }

}
