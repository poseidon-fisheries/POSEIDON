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

package uk.ac.ox.poseidon.core.predicates.numeric;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.DoubleSupplier;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constantDouble;

public class Factories {

    private Factories() {
    }

    public static <S extends Scope> GreaterThanFactory<S> greaterThan(final double threshold) {
        return greaterThan(constantDouble(threshold));
    }

    public static <S extends Scope> GreaterThanFactory<S> greaterThan(
        final Factory<? super S, ? extends DoubleSupplier> threshold
    ) {
        return new GreaterThanFactory<>(threshold);
    }

    public static <S extends Scope> LessThanFactory<S> lessThan(final double threshold) {
        return lessThan(constantDouble(threshold));
    }

    public static <S extends Scope> LessThanFactory<S> lessThan(
        final Factory<? super S, ? extends DoubleSupplier> threshold
    ) {
        return new LessThanFactory<>(threshold);
    }

    public static <S extends Scope> BetweenFactory<S> between(
        final double minimum,
        final double maximum
    ) {
        return between(constantDouble(minimum), constantDouble(maximum));
    }

    public static <S extends Scope> BetweenFactory<S> between(
        final Factory<? super S, ? extends DoubleSupplier> minimum,
        final Factory<? super S, ? extends DoubleSupplier> maximum
    ) {
        return new BetweenFactory<>(minimum, maximum);
    }

}
