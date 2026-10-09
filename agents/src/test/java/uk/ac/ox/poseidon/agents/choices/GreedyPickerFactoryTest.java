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

package uk.ac.ox.poseidon.agents.choices;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.Map;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.choices.Factories.greedyPicker;

class GreedyPickerFactoryTest {

    private final Simulation simulation = simulation();
    private final VesselScope scopeA = scopeOfNewVessel(simulation);
    private final VesselScope scopeB = scopeOfNewVessel(simulation);

    private static Simulation simulation() {
        final Simulation simulation = mock(Simulation.class);
        simulation.random = new MersenneTwisterFast(0);
        return simulation;
    }

    private static VesselScope scopeOfNewVessel(final Simulation simulation) {
        final Vessel vessel = mock(Vessel.class);
        final VesselScope scope = mock(VesselScope.class);
        when(scope.getVessel()).thenReturn(vessel);
        when(scope.getSimulation()).thenReturn(simulation);
        return scope;
    }

    private static class OptionValuesFactory
        extends GlobalScopeFactory<OptionValues<String>> {
        @Override
        protected OptionValues<String> newInstance(final Scope scope) {
            return new ImmutableOptionValues<>(Map.of("A", 1.0, "B", 3.0));
        }
    }

    private static class AnyOptionFactory extends GlobalScopeFactory<Predicate<String>> {
        @Override
        protected Predicate<String> newInstance(final Scope scope) {
            return option -> true;
        }
    }

    @Test
    void givesOnePickerPerVessel() {
        final GreedyPickerFactory<String> factory =
            greedyPicker(new OptionValuesFactory(), new AnyOptionFactory());

        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
        assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
        assertThat(factory.get(scopeA).get()).isEqualTo("B");
    }
}
