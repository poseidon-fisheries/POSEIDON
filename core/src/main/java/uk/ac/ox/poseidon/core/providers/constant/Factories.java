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

package uk.ac.ox.poseidon.core.providers.constant;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

public class Factories {

    private Factories() {
        throw new IllegalStateException("Utility class");
    }

    public static ConstantBooleanProviderFactory alwaysTrue() {
        return new ConstantBooleanProviderFactory(true);
    }

    public static ConstantBooleanProviderFactory alwaysFalse() {
        return new ConstantBooleanProviderFactory(false);
    }

    public static <S extends Scope> ConstantDoubleProviderFromValueFactory<S> constantDouble(
        final double value
    ) {
        return new ConstantDoubleProviderFromValueFactory<>(value);
    }

    public static <S extends Scope> ConstantDoubleProviderFactory<S> constantDouble(
        final Factory<? super S, ? extends Double> doubleFactory
    ) {
        return new ConstantDoubleProviderFactory<>(doubleFactory);
    }

    public static <S extends Scope> ConstantIntProviderFromValueFactory<S> constantInt(
        final int value
    ) {
        return new ConstantIntProviderFromValueFactory<>(value);
    }

    public static <S extends Scope> ConstantIntProviderFactory<S> constantInt(
        final Factory<? super S, ? extends Integer> integerFactory
    ) {
        return new ConstantIntProviderFactory<>(integerFactory);
    }

    public static <S extends Scope, T> ConstantProviderFactory<S, T> constant(
        final Factory<? super S, ? extends T> value
    ) {
        return new ConstantProviderFactory<>(value);
    }

}
