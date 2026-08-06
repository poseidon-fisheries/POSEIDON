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

package uk.ac.ox.poseidon.biology.biomass;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TimeIndexedBiomassGridUpdatesFactoryTest {

    private static final Int2D CELL = new Int2D(0, 0);
    private static final LocalDateTime SIMULATION_START = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final Species HKE = new Species("HKE", null, "Hake");
    private static final Species ANE = new Species("ANE", null, "Anchovy");

    private final ModelGrid modelGrid = ModelGrid.create(1, 1, new Envelope(0, 1, 0, 1));

    @Test
    void onlyTheLatestPreStartSnapshotIsApplied() {
        final DefaultBiomassGrid target = new DefaultBiomassGrid(modelGrid, HKE, 0.0);
        final TemporalSchedule schedule = scheduleBeforeStart();

        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(target)),
            _ -> Map.of(
                LocalDate.of(2020, 1, 1).atStartOfDay(), List.of(snapshot(HKE, 1.0)),
                LocalDate.of(2025, 6, 15).atStartOfDay(), List.of(snapshot(HKE, 2.0))
            )
        );

        factory.get(new SimulationScope(simulation(schedule)));

        verify(schedule, times(1)).scheduleOnce(any(Steppable.class));
        assertThat(target.getValue(CELL)).isEqualTo(2.0);
    }

    @Test
    void futureSnapshotsAreScheduledIndividuallyAtTheirOwnDateTime() {
        final DefaultBiomassGrid target = new DefaultBiomassGrid(modelGrid, HKE, 0.0);
        final TemporalSchedule schedule = scheduleBeforeStart();
        final LocalDate futureDate = LocalDate.of(2026, 3, 1);

        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(target)),
            _ -> Map.of(futureDate.atStartOfDay(), List.of(snapshot(HKE, 5.0)))
        );

        factory.get(new SimulationScope(simulation(schedule)));

        verify(schedule, never()).scheduleOnce(any(Steppable.class));
        final ArgumentCaptor<Steppable> steppable = ArgumentCaptor.forClass(Steppable.class);
        verify(schedule).scheduleOnce(eq(futureDate.atStartOfDay()), steppable.capture());
        assertThat(target.getValue(CELL)).isEqualTo(0.0); // not applied yet

        steppable.getValue().step(null);
        assertThat(target.getValue(CELL)).isEqualTo(5.0);
    }

    @Test
    void futureSnapshotAtNonMidnightTimeIsScheduledAtThatExactTime() {
        // Proves the fractional-day part of a time-indexed snapshot survives all the way to
        // scheduling: this factory must not force updates to midnight via atStartOfDay().
        final DefaultBiomassGrid target = new DefaultBiomassGrid(modelGrid, HKE, 0.0);
        final TemporalSchedule schedule = scheduleBeforeStart();
        final LocalDateTime futureNoon = LocalDateTime.of(2026, 3, 1, 12, 0);

        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(target)),
            _ -> Map.of(futureNoon, List.of(snapshot(HKE, 5.0)))
        );

        factory.get(new SimulationScope(simulation(schedule)));

        verify(schedule).scheduleOnce(eq(futureNoon), any(Steppable.class));
    }

    @Test
    void eachSpeciesUpdatesItsOwnGridOnly() {
        final DefaultBiomassGrid hkeGrid = new DefaultBiomassGrid(modelGrid, HKE, 0.0);
        final DefaultBiomassGrid aneGrid = new DefaultBiomassGrid(modelGrid, ANE, 0.0);
        final TemporalSchedule schedule = scheduleBeforeStart();

        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(hkeGrid, aneGrid)),
            _ -> Map.of(LocalDate.of(2020, 1, 1).atStartOfDay(), List.of(snapshot(HKE, 3.0), snapshot(ANE, 7.0)))
        );

        factory.get(new SimulationScope(simulation(schedule)));

        assertThat(hkeGrid.getValue(CELL)).isEqualTo(3.0);
        assertThat(aneGrid.getValue(CELL)).isEqualTo(7.0);
    }

    @Test
    void returnsTheSameFisheableBiomassGridsPassedIn() {
        final FisheableBiomassGrids target =
            new FisheableBiomassGrids(List.of(new DefaultBiomassGrid(modelGrid, HKE, 0.0)));
        final TemporalSchedule schedule = scheduleBeforeStart();

        final TimeIndexedBiomassGridUpdatesFactory factory =
            new TimeIndexedBiomassGridUpdatesFactory(_ -> target, _ -> Map.of());

        assertThat(factory.get(new SimulationScope(simulation(schedule)))).isSameAs(target);
    }

    private ImmutableBiomassGrid snapshot(
        final Species species,
        final double value
    ) {
        return new ImmutableBiomassGrid(modelGrid, species, new double[][] {{value}});
    }

    /**
     * A schedule positioned before the simulation starts, i.e. {@code getTime() < EPOCH}, which
     * is how a factory resolved while building the scenario (before any simulation step has run)
     * actually behaves.
     */
    private static TemporalSchedule scheduleBeforeStart() {
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getTime()).thenReturn(TemporalSchedule.EPOCH - 1);
        when(schedule.toDateTime(TemporalSchedule.EPOCH)).thenReturn(SIMULATION_START);
        doAnswer(invocation -> {
            final Steppable steppable = invocation.getArgument(0);
            steppable.step(null);
            return true;
        }).when(schedule).scheduleOnce(any(Steppable.class));
        return schedule;
    }

    private static Simulation simulation(final TemporalSchedule schedule) {
        final Simulation simulation = mock(Simulation.class);
        when(simulation.getTemporalSchedule()).thenReturn(schedule);
        return simulation;
    }

}
