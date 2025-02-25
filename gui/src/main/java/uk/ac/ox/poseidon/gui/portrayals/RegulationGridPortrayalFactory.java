/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.gui.portrayals;

import lombok.*;
import sim.field.grid.ObjectGrid2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.grid.ObjectGridPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import uk.ac.ox.poseidon.agents.behaviours.fishing.DummyFishingAction;
import uk.ac.ox.poseidon.agents.fleets.Fleet;
import uk.ac.ox.poseidon.agents.regulations.Regulations;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalField;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;
import static uk.ac.ox.poseidon.gui.portrayals.RegulationGridPortrayalFactory.UpdateFrequency.EVERY_MONTH;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegulationGridPortrayalFactory extends SimulationScopeFactory<ObjectGridPortrayal2D> {

    private Factory<? extends Regulations> regulations;
    private Factory<? extends Fleet> fleet;
    private Factory<? extends BathymetricGrid> bathymetric;
    private int displayWidth;
    private int displayHeight;

    @Override
    protected ObjectGridPortrayal2D newInstance(final @NonNull Simulation simulation) {
        return new Portrayal(
            simulation.getTemporalSchedule(),
            regulations.get(simulation),
            fleet.get(simulation),
            bathymetric.get(simulation),
            EVERY_MONTH,
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
        private final Regulations regulations;
        private final Fleet fleet;
        private final BathymetricGrid bathymetricGrid;
        private final ObjectGrid2D grid;
        private final UpdateFrequency updateFrequency;
        private long lastUpdated;

        Portrayal(
            final TemporalSchedule schedule,
            final Regulations regulations,
            final Fleet fleet,
            final BathymetricGrid bathymetricGrid,
            final UpdateFrequency updateFrequency,
            final int displayWidth,
            final int displayHeight
        ) {
            super();
            this.regulations = regulations;
            this.fleet = fleet;
            this.bathymetricGrid = bathymetricGrid;
            final int gridWidth = bathymetricGrid.getField().width;
            final int gridHeight = bathymetricGrid.getField().height;
            this.grid = new ObjectGrid2D(gridWidth, gridHeight);
            this.schedule = schedule;
            this.updateFrequency = updateFrequency;
            setField(grid);

            final Image scaledImage =
                texture.getScaledInstance(
                    displayWidth / gridWidth,
                    displayHeight / gridHeight,
                    Image.SCALE_SMOOTH
                );

            setPortrayalForNonNull(new ImagePortrayal2D(scaledImage));
        }

        @Override
        public void draw(
            final Object object,
            final Graphics2D graphics,
            final DrawInfo2D info
        ) {
            final long currentFieldValue = schedule.getDate().getLong(updateFrequency.field);
            if (currentFieldValue != lastUpdated) {
                updateGrid();
                lastUpdated = currentFieldValue;
            }
            super.draw(object, graphics, info);
        }

        void updateGrid() {
            bathymetricGrid.getActiveWaterCells().forEach(cell -> {
                final LocalDateTime dateTime = schedule.getDateTime();
                final boolean forbidden =
                    fleet.getVessels()
                        .stream()
                        .map(vessel ->
                            new DummyFishingAction(
                                dateTime,
                                vessel,
                                bathymetricGrid.getModelGrid().toCoordinate(cell)
                            )
                        ).anyMatch(regulations::isForbidden);
                grid.field[cell.x][cell.y] = forbidden ? "FORBIDDEN" : null;
            });
        }

    }

}
