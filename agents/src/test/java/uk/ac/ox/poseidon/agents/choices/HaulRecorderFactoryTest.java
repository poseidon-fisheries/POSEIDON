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
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.vessels.PerVesselFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.events.EventManager;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.function.Function;
import java.util.function.ToDoubleBiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.choices.Factories.exponentialMovingAverageOfBuckets;
import static uk.ac.ox.poseidon.agents.choices.Factories.haulRecorder;
import static uk.ac.ox.poseidon.agents.choices.Factories.keyedMemory;
import static uk.ac.ox.poseidon.agents.choices.Factories.keyedMemorySelector;
import static uk.ac.ox.poseidon.agents.choices.Factories.memoryBasedOptionValues;
import static uk.ac.ox.poseidon.agents.choices.HaulRecorderTest.haul;
import static uk.ac.ox.poseidon.agents.tasks.fishing.Factories.fishingEventProperty;
import static uk.ac.ox.poseidon.agents.vessels.Factories.perVessel;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

class HaulRecorderFactoryTest {

    private static final Species SPECIES = new Species("A", null, "A");
    private static final Coordinate COORDINATE = new Coordinate(1.5, 41.5);
    private static final Int2D CELL = new Int2D(3, 4);

    private final Simulation simulation = mock(Simulation.class);
    private final VesselScope scopeA = scopeOfNewVessel(simulation);
    private final VesselScope scopeB = scopeOfNewVessel(simulation);
    private final PerVesselFactory<KeyedMemory<String, Int2D, Bucket>> keyedMemory =
        perVessel(keyedMemory());
    private final HaulRecorderFactory<VesselScope> factory = haulRecorder(
        object(modelGrid()),
        keyedMemorySelector(keyedMemory, fishingEventProperty("action.gear.code")),
        exponentialMovingAverageOfBuckets(0.5)
    );

    private static VesselScope scopeOfNewVessel(final Simulation simulation) {
        final EventManager eventManager = new SimpleEventManager();
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getEventManager()).thenReturn(eventManager);
        final VesselScope scope = mock(VesselScope.class);
        when(scope.getVessel()).thenReturn(vessel);
        when(scope.getSimulation()).thenReturn(simulation);
        return scope;
    }

    private static ModelGrid modelGrid() {
        final ModelGrid modelGrid = mock(ModelGrid.class);
        when(modelGrid.toCell(COORDINATE)).thenReturn(CELL);
        return modelGrid;
    }

    private static class PurseSeineKeyFactory
        extends GlobalScopeFactory<Function<Vessel, String>> {
        @Override
        protected Function<Vessel, String> newInstance(final Scope scope) {
            return vessel -> "PS";
        }
    }

    private static class KgOfSpeciesValuationFactory
        extends GlobalScopeFactory<ToDoubleBiFunction<Int2D, Bucket>> {
        @Override
        protected ToDoubleBiFunction<Int2D, Bucket> newInstance(final Scope scope) {
            return (cell, bucket) -> bucket.getKg(SPECIES);
        }
    }

    @Test
    void doesNotRegisterTheRecorderOnTheVesselsEventManager() {
        factory.get(scopeA);
        scopeA.getVessel()
            .getEventManager()
            .broadcast(haul("PS", COORDINATE, Bucket.of(SPECIES, 10)));

        assertThat(keyedMemory.get(scopeA).get("PS").get(CELL)).isEmpty();
    }

    @Test
    void recordsInTheMemoryTheVesselsOptionValuesRead() {
        final MemoryBasedOptionValuesFactory<Int2D, Bucket> optionValues = memoryBasedOptionValues(
            keyedMemorySelector(keyedMemory, new PurseSeineKeyFactory()),
            new KgOfSpeciesValuationFactory()
        );

        factory.get(scopeA).receive(haul("PS", COORDINATE, Bucket.of(SPECIES, 10)));

        assertThat(optionValues.get(scopeA).getValue(CELL)).contains(10.0);
        assertThat(optionValues.get(scopeB).getValue(CELL)).isEmpty();
    }

    @Test
    void givesOneRecorderPerVessel() {
        assertThat(factory.get(scopeA)).isNotNull().isSameAs(factory.get(scopeA));
        assertThat(factory.get(scopeA)).isNotSameAs(factory.get(scopeB));
    }

    @Test
    void sharesOneRecorderBetweenVesselsSharingAMemory() {
        final HaulRecorderFactory<VesselScope> sharedFactory = haulRecorder(
            object(modelGrid()),
            keyedMemorySelector(keyedMemory(), fishingEventProperty("action.gear.code")),
            exponentialMovingAverageOfBuckets(0.5)
        );

        assertThat(sharedFactory.get(scopeA)).isNotNull().isSameAs(sharedFactory.get(scopeB));
    }
}
