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

package uk.ac.ox.poseidon.core.suppliers;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import static uk.ac.ox.poseidon.core.utils.Factories.object;

public final class Factories {
    private Factories() {
        throw new IllegalStateException("Utility class");
    }

    public static <S extends Scope> ConstantDoubleSupplierFactory<S> constantDouble(
        final Factory<? super S, ? extends Number> value
    ) {
        return new ConstantDoubleSupplierFactory<>(value);
    }

    public static ConstantDoubleSupplierFactory<Scope> constantDouble(final double value) {
        return new ConstantDoubleSupplierFactory<>(object(value));
    }

    public static ConstantIntSupplierFactory<Scope> constantInt(final int value) {
        return new ConstantIntSupplierFactory<>(object(value));
    }

    public static <S extends Scope> ConstantIntSupplierFactory<S> constantInt(
        final Factory<? super S, ? extends Number> value
    ) {
        return new ConstantIntSupplierFactory<>(value);
    }

    public static RandomDoubleSupplierFactory randomDouble(
        final double minimum,
        final double maximum
    ) {
        return new RandomDoubleSupplierFactory(minimum, maximum);
    }

    public static RandomIntSupplierFactory randomInt(
        final int minimum,
        final int maximum
    ) {
        return new RandomIntSupplierFactory(minimum, maximum);
    }

    public static <S extends Scope, T> ConstantSupplierFactory<S, T> always(
        final Factory<? super S, ? extends T> value
    ) {
        return new ConstantSupplierFactory<>(value);
    }

}
