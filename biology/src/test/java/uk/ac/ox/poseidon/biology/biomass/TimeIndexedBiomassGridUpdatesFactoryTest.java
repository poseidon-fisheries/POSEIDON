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
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class TimeIndexedBiomassGridUpdatesFactoryTest {

    private static final Int2D CELL = new Int2D(0, 0);
    private static final Species HKE = new Species("HKE", null, "Hake");
    private static final Species ANE = new Species("ANE", null, "Anchovy");

    private final ModelGrid modelGrid = ModelGrid.create(1, 1, new Envelope(0, 1, 0, 1));

    private final Simulation simulation = mock(Simulation.class);

    @Test
    void datesEachUpdateAtItsSnapshotDateTime() {
        // A non-midnight date-time proves the time of day survives: this factory must not force
        // updates to midnight via atStartOfDay().
        final LocalDateTime midnight = LocalDate.of(2020, 1, 1).atStartOfDay();
        final LocalDateTime noon = LocalDateTime.of(2026, 3, 1, 12, 0);
        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(new DefaultBiomassGrid(modelGrid, HKE, 0.0))),
            _ -> Map.of(
                midnight, List.of(snapshot(HKE, 1.0)),
                noon, List.of(snapshot(HKE, 2.0))
            )
        );

        assertThat(factory.get(scope()))
            .extracting(Entry::getKey)
            .containsExactlyInAnyOrder(midnight, noon);
    }

    @Test
    void updatesReplaceGridContentsOnlyWhenStepped() {
        final DefaultBiomassGrid target = new DefaultBiomassGrid(modelGrid, HKE, 0.0);
        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(target)),
            _ -> Map.of(LocalDate.of(2026, 3, 1).atStartOfDay(), List.of(snapshot(HKE, 5.0)))
        );

        final List<Entry<LocalDateTime, BiomassGridUpdate>> updates = factory.get(scope());
        assertThat(target.getValue(CELL)).isEqualTo(0.0);

        updates.forEach(update -> update.getValue().step(null));
        assertThat(target.getValue(CELL)).isEqualTo(5.0);
    }

    @Test
    void eachSpeciesUpdatesItsOwnGridOnly() {
        final DefaultBiomassGrid hkeGrid = new DefaultBiomassGrid(modelGrid, HKE, 0.0);
        final DefaultBiomassGrid aneGrid = new DefaultBiomassGrid(modelGrid, ANE, 0.0);
        final TimeIndexedBiomassGridUpdatesFactory factory = new TimeIndexedBiomassGridUpdatesFactory(
            _ -> new FisheableBiomassGrids(List.of(hkeGrid, aneGrid)),
            _ -> Map.of(
                LocalDate.of(2020, 1, 1).atStartOfDay(),
                List.of(snapshot(HKE, 3.0), snapshot(ANE, 7.0))
            )
        );

        factory.get(scope()).forEach(update -> update.getValue().step(null));

        assertThat(hkeGrid.getValue(CELL)).isEqualTo(3.0);
        assertThat(aneGrid.getValue(CELL)).isEqualTo(7.0);
    }

    private ImmutableBiomassGrid snapshot(
        final Species species,
        final double value
    ) {
        return new ImmutableBiomassGrid(modelGrid, species, new double[][] {{value}});
    }

    private SimulationScope scope() {
        return new SimulationScope(simulation);
    }

}
