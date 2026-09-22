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

package uk.ac.ox.poseidon.core.providers;

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Factories for {@link Provider}s that snapshot another factory's value once at build time, and
 * for a {@link Provider} that shifts a delegate's int value by a fixed amount.
 */
public final class Factories {

    private Factories() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param delegate factory for the supplier whose value is resolved once and snapshotted
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link Provider} of
     * that snapshotted value
     * @see FirstValueFromFactory
     */
    public static <S extends Scope, T> FirstValueFromFactory<S, T> firstValueFrom(
        final Factory<? super S, ? extends Supplier<? extends T>> delegate
    ) {
        return new FirstValueFromFactory<>(delegate);
    }

    /**
     * @param delegate factory for the int supplier whose value is resolved once and snapshotted
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link IntProvider} of
     * that snapshotted value
     * @see FirstIntFromFactory
     */
    public static <S extends Scope> FirstIntFromFactory<S> firstIntFrom(
        final Factory<? super S, ? extends IntSupplier> delegate
    ) {
        return new FirstIntFromFactory<>(delegate);
    }

    /**
     * @param delegate factory for the double supplier whose value is resolved once and
     * snapshotted
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link DoubleProvider} of that snapshotted value
     * @see FirstDoubleFromFactory
     */
    public static <S extends Scope> FirstDoubleFromFactory<S> firstDoubleFrom(
        final Factory<? super S, ? extends DoubleSupplier> delegate
    ) {
        return new FirstDoubleFromFactory<>(delegate);
    }

    /**
     * @param delegate factory for the boolean supplier whose value is resolved once and
     * snapshotted
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link BooleanProvider} of that snapshotted value
     * @see FirstBooleanFromFactory
     */
    public static <S extends Scope> FirstBooleanFromFactory<S> firstBooleanFrom(
        final Factory<? super S, ? extends BooleanSupplier> delegate
    ) {
        return new FirstBooleanFromFactory<>(delegate);
    }

    /**
     * @param delegateIntProvider factory for the int provider to shift
     * @param shift               the amount to add to the delegate's value on every call
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link ShiftedIntProvider} over the resolved delegate
     * @see ShiftedIntProvider
     */
    public static <S extends Scope> ShiftedIntProviderFactory<S> shiftedInt(
        final Factory<? super S, ? extends IntProvider> delegateIntProvider,
        final int shift
    ) {
        return new ShiftedIntProviderFactory<>(delegateIntProvider, shift);
    }

}
