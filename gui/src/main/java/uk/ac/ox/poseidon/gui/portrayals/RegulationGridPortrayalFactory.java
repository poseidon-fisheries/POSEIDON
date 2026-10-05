/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

import lombok.*;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.regulations.Regulations;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

import static java.time.temporal.ChronoField.*;
import static uk.ac.ox.poseidon.core.MasonUtils.bagToStream;
import static uk.ac.ox.poseidon.gui.portrayals.RegulationGridPortrayalFactory.UpdateFrequency.EVERY_DAY;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
/**
 * A {@link SimulationScopeFactory} that overlays a checkered texture on cells where fishing is
 * currently forbidden for every active vessel of a fleet, i.e. cells closed to the whole fleet,
 * recomputed once per {@link UpdateFrequency#EVERY_DAY day} rather than on every draw. Cells
 * closed to only part of the fleet, e.g. to the vessels of a port under a temporary closure, are
 * not marked. If no vessel of the fleet is active, no cell is marked.
 */
public class RegulationGridPortrayalFactory extends SimulationScopeFactory<ObjectGridPortrayal2D> {

    private Factory<? super SimulationScope, ? extends Regulations<? super ExtendedFishingAction>>
        regulations;
    private Factory<? super SimulationScope, ? extends VesselField> vesselField;
    private Factory<? super SimulationScope, ? extends Predicate<? super Vessel>> fleet;
    private Factory<? super SimulationScope, ? extends BathymetricGrid> bathymetric;
    private int displayWidth;
    private int displayHeight;

    /** @return a portrayal overlaying a checkered texture on cells currently forbidden to fish */
    @Override
    protected ObjectGridPortrayal2D newInstance(final SimulationScope scope) {
        return new Portrayal(
            scope.getSimulation().getTemporalSchedule(),
            regulations.get(scope),
            vesselField.get(scope),
            fleet.get(scope),
            bathymetric.get(scope),
            EVERY_DAY,
            displayWidth,
            displayHeight
        );
    }

    @RequiredArgsConstructor
    enum UpdateFrequency {
        EVERY_DAY(EPOCH_DAY),
        EVERY_MONTH(PROLEPTIC_MONTH),
        EVERY_YEAR(YEAR);
        private final TemporalField field;
    }

    @Getter
    static class Portrayal extends ObjectGridPortrayal2D {

        private static final Image texture;

        static {
            try {
                texture = ImageIO.read(Objects.requireNonNull(
                    RegulationGridPortrayalFactory.class.getResource(
                        "/images/checkered.png"
                    )
                ));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        }

        private final TemporalSchedule schedule;
        private final Regulations<? super ExtendedFishingAction> regulations;
        private final VesselField vesselField;
        private final Predicate<? super Vessel> fleet;
        private final BathymetricGrid bathymetricGrid;
        private final ObjectGrid2D grid;
        private final UpdateFrequency updateFrequency;
        private long lastUpdated;

        Portrayal(
            final TemporalSchedule schedule,
            final Regulations<? super ExtendedFishingAction> regulations,
            final VesselField vesselField,
            final Predicate<? super Vessel> fleet,
            final BathymetricGrid bathymetricGrid,
            final UpdateFrequency updateFrequency,
            final int displayWidth,
            final int displayHeight
        ) {
            super();
            this.regulations = regulations;
            this.vesselField = vesselField;
            this.fleet = fleet;
            this.bathymetricGrid = bathymetricGrid;
            final int gridWidth = bathymetricGrid.getModelGrid().getGridWidth();
            final int gridHeight = bathymetricGrid.getModelGrid().getGridHeight();
            this.grid = new ObjectGrid2D(gridWidth, gridHeight);
            this.schedule = schedule;
            this.updateFrequency = updateFrequency;
            setField(grid);

            final Image scaledImage =
                texture.getScaledInstance(
                    displayWidth / gridWidth,
                    displayHeight / gridHeight,
                    Image.SCALE_DEFAULT
                );

            setPortrayalForNonNull(new ImagePortrayal2D(scaledImage));
        }

        @Override
        public void draw(
            final Object object,
            final Graphics2D graphics,
            final DrawInfo2D info
        ) {
            final double time = schedule.getTime();
            if (time >= TemporalSchedule.EPOCH && time < Double.POSITIVE_INFINITY) {
                final long currentFieldValue = schedule.getDate().getLong(updateFrequency.field);
                if (currentFieldValue != lastUpdated) {
                    updateGrid();
                    lastUpdated = currentFieldValue;
                }
            }
            super.draw(object, graphics, info);
        }

        void updateGrid() {
            final LocalDateTime dateTime = schedule.getDateTime();
            final List<Vessel> fleetVessels =
                bagToStream(vesselField.getField().allObjects, Vessel.class)
                    .filter(Vessel::isActive)
                    .filter(fleet)
                    .toList();
            bathymetricGrid.getActiveWaterCells().forEach(cell -> {
                final boolean forbidden =
                    !fleetVessels.isEmpty() &&
                        fleetVessels
                            .stream()
                            .map(vessel ->
                                new ExtendedFishingAction(
                                    vessel,
                                    dateTime,
                                    Duration.ZERO,
                                    bathymetricGrid.getModelGrid().toCoordinate(cell),
                                    vessel.getGear()
                                )
                            )
                            .allMatch(regulations::isForbidden);
                grid.field[cell.x][cell.y] = forbidden ? "FORBIDDEN" : null;
            });
        }

    }

}
