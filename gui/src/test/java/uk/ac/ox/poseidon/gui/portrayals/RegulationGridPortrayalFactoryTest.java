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

package uk.ac.ox.poseidon.gui.portrayals;

import org.junit.jupiter.api.Test;
import sim.field.continuous.Continuous2D;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.regulations.Regulations;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.gui.portrayals.RegulationGridPortrayalFactory.UpdateFrequency.EVERY_DAY;

class RegulationGridPortrayalFactoryTest {

    private static final Int2D CLOSED_TO_FLEET = new Int2D(0, 0);
    private static final Int2D CLOSED_TO_ONE_VESSEL = new Int2D(1, 0);
    private static final Int2D OPEN = new Int2D(2, 0);

    private final ModelGrid modelGrid = ModelGrid.create(3, 1, new Envelope(0, 3, 0, 1));
    private final Vessel seiner1 = vessel();
    private final Vessel seiner2 = vessel();
    private final Vessel trawler = vessel();

    @Test
    void marksOnlyCellsClosedToEveryVesselOfTheFleet() {
        final RegulationGridPortrayalFactory.Portrayal portrayal =
            portrayal(Set.of(seiner1, seiner2)::contains);

        portrayal.updateGrid();

        assertThat(portrayal.getGrid().field[0][0]).isEqualTo("FORBIDDEN");
        assertThat(portrayal.getGrid().field[1][0]).isNull();
        assertThat(portrayal.getGrid().field[2][0]).isNull();
    }

    @Test
    void marksNothingWhenNoVesselOfTheFleetIsActive() {
        final RegulationGridPortrayalFactory.Portrayal portrayal = portrayal(_ -> false);

        portrayal.updateGrid();

        assertThat(portrayal.getGrid().field[0][0]).isNull();
    }

    /**
     * The trawler is forbidden everywhere and seiner1 also in {@link #CLOSED_TO_ONE_VESSEL}, so
     * only {@link #CLOSED_TO_FLEET} is closed to both seiners.
     */
    private RegulationGridPortrayalFactory.Portrayal portrayal(final Predicate<Vessel> fleet) {
        final Regulations<ExtendedFishingAction> regulations = action -> {
            final Int2D cell = modelGrid.toCell(action.getStartCoordinate());
            final Vessel vessel = action.getAgent();
            return !(vessel == trawler || cell.equals(CLOSED_TO_FLEET) ||
                (vessel == seiner1 && cell.equals(CLOSED_TO_ONE_VESSEL)));
        };
        final Continuous2D field = new Continuous2D(1, 3, 1);
        Stream.of(seiner1, seiner2, trawler).forEach(v -> field.setObjectLocation(v, new Double2D()));
        final VesselField vesselField = mock(VesselField.class);
        when(vesselField.getField()).thenReturn(field);
        final BathymetricGrid bathymetricGrid = mock(BathymetricGrid.class);
        when(bathymetricGrid.getModelGrid()).thenReturn(modelGrid);
        when(bathymetricGrid.getActiveWaterCells())
            .thenAnswer(_ -> Stream.of(CLOSED_TO_FLEET, CLOSED_TO_ONE_VESSEL, OPEN));
        final TemporalSchedule schedule = mock(TemporalSchedule.class);
        when(schedule.getDateTime()).thenReturn(LocalDateTime.of(2013, 1, 2, 0, 0));
        return new RegulationGridPortrayalFactory.Portrayal(
            schedule, regulations, vesselField, fleet, bathymetricGrid, EVERY_DAY, 30, 10
        );
    }

    private static Vessel vessel() {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.isActive()).thenReturn(true);
        when(vessel.getGear()).thenReturn(mock(Gear.class));
        return vessel;
    }
}
