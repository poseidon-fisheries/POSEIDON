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

package uk.ac.ox.poseidon.core.providers.math;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;

public class Factories {
    
    private Factories() {}

    public static <S extends Scope> MaxDoubleFactory<S> maxDouble(
        final Factory<? super S, ? extends DoubleSupplier> a,
        final Factory<? super S, ? extends DoubleSupplier> b
    ) {
        return new MaxDoubleFactory<>(a, b);
    }

    public static <S extends Scope> MinDoubleFactory<S> minDouble(
        final Factory<? super S, ? extends DoubleSupplier> a,
        final Factory<? super S, ? extends DoubleSupplier> b
    ) {
        return new MinDoubleFactory<>(a, b);
    }

    public static <S extends Scope> MaxIntFactory<S> maxInt(
        final Factory<? super S, ? extends IntSupplier> a,
        final Factory<? super S, ? extends IntSupplier> b
    ) {
        return new MaxIntFactory<>(a, b);
    }

    public static <S extends Scope> MinIntFactory<S> minInt(
        final Factory<? super S, ? extends IntSupplier> a,
        final Factory<? super S, ? extends IntSupplier> b
    ) {
        return new MinIntFactory<>(a, b);
    }

}
