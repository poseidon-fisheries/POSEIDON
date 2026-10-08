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

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.PerVesselFactory;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.choices.Factories.keyedMemory;
import static uk.ac.ox.poseidon.agents.choices.Factories.keyedMemorySelector;
import static uk.ac.ox.poseidon.agents.choices.Factories.memory;
import static uk.ac.ox.poseidon.agents.choices.Factories.memoryBasedOptionValues;
import static uk.ac.ox.poseidon.agents.vessels.Factories.perVessel;

class MemoryFactoriesTest {

    private final Simulation simulation = mock(Simulation.class);
    private final VesselScope scopeA = scopeOfNewVessel(simulation);
    private final VesselScope scopeB = scopeOfNewVessel(simulation);

    private static VesselScope scopeOfNewVessel(final Simulation simulation) {
        final Vessel vessel = mock(Vessel.class);
        final VesselScope scope = mock(VesselScope.class);
        when(scope.getVessel()).thenReturn(vessel);
        when(scope.getSimulation()).thenReturn(simulation);
        return scope;
    }

    private static class FirstLetterFactory
        extends GlobalScopeFactory<Function<String, String>> {
        @Override
        protected Function<String, String> newInstance(final Scope scope) {
            return input -> input.substring(0, 1);
        }
    }

    private static class UpperCaseFactory
        extends GlobalScopeFactory<Function<String, String>> {
        @Override
        protected Function<String, String> newInstance(final Scope scope) {
            return String::toUpperCase;
        }
    }

    private static class TrawlKeyFactory extends GlobalScopeFactory<Function<Vessel, String>> {
        @Override
        protected Function<Vessel, String> newInstance(final Scope scope) {
            return vessel -> "trawl";
        }
    }

    private static class RecollectionValuationFactory
        extends GlobalScopeFactory<ToDoubleBiFunction<String, Double>> {
        @Override
        protected ToDoubleBiFunction<String, Double> newInstance(final Scope scope) {
            return (option, recollection) -> recollection;
        }
    }

    @Test
    void memoryIsSharedBySimulationUnlessMadePerVessel() {
        final MemoryFactory<String, Double> factory = memory();
        final PerVesselFactory<Memory<String, Double>> perVesselFactory = perVessel(factory);
        final VesselScope scopeInOtherSimulation = scopeOfNewVessel(mock(Simulation.class));

        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeB));
        assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeInOtherSimulation));
        assertThat(perVesselFactory.get(scopeA)).isSameAs(perVesselFactory.get(scopeA));
        assertThat(perVesselFactory.get(scopeA)).isNotSameAs(perVesselFactory.get(scopeB));
    }

    @Test
    void keyedMemoryIsSharedBySimulationUnlessMadePerVessel() {
        final KeyedMemoryFactory<String, String, Double> factory = keyedMemory();
        final PerVesselFactory<KeyedMemory<String, String, Double>> perVesselFactory =
            perVessel(factory);
        final VesselScope scopeInOtherSimulation = scopeOfNewVessel(mock(Simulation.class));

        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeB));
        assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeInOtherSimulation));
        assertThat(perVesselFactory.get(scopeA)).isSameAs(perVesselFactory.get(scopeA));
        assertThat(perVesselFactory.get(scopeA)).isNotSameAs(perVesselFactory.get(scopeB));
    }

    @Test
    void selectorIsOnePerVesselWhenItsKeyedMemoryIs() {
        final KeyedMemorySelectorFactory<VesselScope, String, String, String, Double> factory =
            keyedMemorySelector(perVessel(keyedMemory()), new FirstLetterFactory());

        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
        assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    }

    @Test
    void selectorsSharingAPerVesselKeyedMemorySelectFromTheSameMemory() {
        final PerVesselFactory<KeyedMemory<String, String, Double>> keyedMemory =
            perVessel(keyedMemory());
        final KeyedMemorySelectorFactory<VesselScope, String, String, String, Double> writer =
            keyedMemorySelector(keyedMemory, new FirstLetterFactory());
        final KeyedMemorySelectorFactory<VesselScope, String, String, String, Double> reader =
            keyedMemorySelector(keyedMemory, new UpperCaseFactory());

        assertThat(writer.get(scopeA).apply("Trawl")).isSameAs(reader.get(scopeA).apply("t"));
        assertThat(writer.get(scopeA).apply("Trawl")).isNotSameAs(reader.get(scopeB).apply("t"));
    }

    @Test
    void separatePerVesselWrappersGiveSeparateMemories() {
        final KeyedMemoryFactory<String, String, Double> keyedMemory = keyedMemory();
        final KeyedMemorySelectorFactory<VesselScope, String, String, String, Double> writer =
            keyedMemorySelector(perVessel(keyedMemory), new FirstLetterFactory());
        final KeyedMemorySelectorFactory<VesselScope, String, String, String, Double> reader =
            keyedMemorySelector(perVessel(keyedMemory), new UpperCaseFactory());

        assertThat(writer.get(scopeA).apply("Trawl")).isNotSameAs(reader.get(scopeA).apply("t"));
    }

    @Test
    void optionValuesReadTheMemoryTheVesselsSelectorGives() {
        final PerVesselFactory<KeyedMemory<String, String, Double>> keyedMemory =
            perVessel(keyedMemory());
        final MemoryBasedOptionValuesFactory<String, Double> factory = memoryBasedOptionValues(
            keyedMemorySelector(keyedMemory, new TrawlKeyFactory()),
            new RecollectionValuationFactory()
        );
        keyedMemory.get(scopeA).get("trawl").observe("X", 3.0, Double::sum);

        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
        assertThat(factory.get(scopeA).getValue("X")).contains(3.0);
        assertThat(factory.get(scopeB).getValue("X")).isEmpty();
    }
}
