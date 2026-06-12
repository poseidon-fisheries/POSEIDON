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

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.assertj.core.api.Assertions.assertThat;

class FirstBooleanFromFactoryTest {

    private static class CallCountingSupplier implements BooleanSupplier {
        final AtomicInteger callCount = new AtomicInteger(0);
        private boolean value = true;

        @Override
        public boolean getAsBoolean() {
            callCount.incrementAndGet();
            final boolean old = value;
            value = !value;
            return old;
        }
    }

    @Test
    void returnedProviderIsStable() {
        final CallCountingSupplier supplier = new CallCountingSupplier();
        final FirstBooleanFromFactory<Scope> factory =
            new FirstBooleanFromFactory<>(
                new GlobalScopeFactory<BooleanSupplier>() {
                    @Override
                    protected BooleanSupplier newInstance(final Scope scope) {
                        return supplier;
                    }
                }
            );

        final BooleanProvider provider = factory.get(Scope.GLOBAL_SCOPE);
        assertThat(provider.getAsBoolean()).isTrue();
        assertThat(provider.getAsBoolean()).isTrue();
        assertThat(provider.getAsBoolean()).isTrue();
    }

    @Test
    void delegateSupplierCalledOnlyOnce() {
        final CallCountingSupplier supplier = new CallCountingSupplier();
        final FirstBooleanFromFactory<Scope> factory =
            new FirstBooleanFromFactory<>(
                new GlobalScopeFactory<BooleanSupplier>() {
                    @Override
                    protected BooleanSupplier newInstance(final Scope scope) {
                        return supplier;
                    }
                }
            );

        factory.get(Scope.GLOBAL_SCOPE);
        assertThat(supplier.callCount).hasValue(1);
    }

    @Test
    void returnsConstantBooleanProvider() {
        final FirstBooleanFromFactory<Scope> factory =
            new FirstBooleanFromFactory<>(
                new GlobalScopeFactory<BooleanSupplier>() {
                    @Override
                    protected BooleanSupplier newInstance(final Scope scope) {
                        return () -> false;
                    }
                }
            );

        assertThat(factory.get(Scope.GLOBAL_SCOPE).getAsBoolean()).isFalse();
    }
}
