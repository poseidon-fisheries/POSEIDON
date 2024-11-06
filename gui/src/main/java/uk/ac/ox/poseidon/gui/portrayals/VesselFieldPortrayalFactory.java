/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

import static java.awt.Color.BLACK;
import static sim.portrayal.simple.OrientedPortrayal2D.SHAPE_COMPASS;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VesselFieldPortrayalFactory extends SimulationScopeFactory<ContinuousPortrayal2D> {

    private static final double SCALE = 0.5;
    private Factory<? extends VesselField> vesselField;

    @Override
    protected ContinuousPortrayal2D newInstance(final Simulation simulation) {
        final ContinuousPortrayal2D continuousPortrayal2D = new ContinuousPortrayal2D();
        continuousPortrayal2D.setField(vesselField.get(simulation).getField());
        continuousPortrayal2D.setPortrayalForAll(
            new OrientedPortrayal2D(
                new SimplePortrayal2D(),
                0,
                SCALE,
                BLACK,
                SHAPE_COMPASS
            )
        );
        return continuousPortrayal2D;
    }
}
