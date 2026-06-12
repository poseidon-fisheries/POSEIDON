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
import uk.ac.ox.poseidon.core.providers.constant.ConstantProvider;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

class FirstValueFromFactoryTest {

    @Test
    void returnedProviderIsStable() {
        final AtomicInteger counter = new AtomicInteger(0);
        final FirstValueFromFactory<Scope, Integer> factory =
            new FirstValueFromFactory<>(
                new GlobalScopeFactory<Supplier<? extends Integer>>() {
                    @Override
                    protected Supplier<? extends Integer> newInstance(final Scope scope) {
                        return counter::incrementAndGet;
                    }
                }
            );

        final Provider<Integer> provider = factory.get(Scope.GLOBAL_SCOPE);
        assertThat(provider.get()).isEqualTo(1);
        assertThat(provider.get()).isEqualTo(1);
        assertThat(provider.get()).isEqualTo(1);
    }

    @Test
    void delegateSupplierCalledOnlyOnce() {
        final AtomicInteger counter = new AtomicInteger(0);
        final FirstValueFromFactory<Scope, Integer> factory =
            new FirstValueFromFactory<>(
                new GlobalScopeFactory<Supplier<? extends Integer>>() {
                    @Override
                    protected Supplier<? extends Integer> newInstance(final Scope scope) {
                        return counter::incrementAndGet;
                    }
                }
            );

        factory.get(Scope.GLOBAL_SCOPE);
        assertThat(counter).hasValue(1);
    }

    @Test
    void returnsConstantProvider() {
        final FirstValueFromFactory<Scope, String> factory =
            new FirstValueFromFactory<>(
                new GlobalScopeFactory<Supplier<? extends String>>() {
                    @Override
                    protected Supplier<? extends String> newInstance(final Scope scope) {
                        return () -> "hello";
                    }
                }
            );

        assertThat(factory.get(Scope.GLOBAL_SCOPE))
            .isInstanceOf(ConstantProvider.class)
            .extracting(Provider::get)
            .isEqualTo("hello");
    }
}
