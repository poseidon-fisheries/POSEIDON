/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import sim.portrayal.SimplePortrayal2D;
import sim.portrayal.continuous.ContinuousPortrayal2D;
import sim.portrayal.simple.OrientedPortrayal2D;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

import static java.awt.Color.LIGHT_GRAY;
import static sim.portrayal.simple.OrientedPortrayal2D.SHAPE_COMPASS;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class VesselFieldPortrayalFactory extends SimulationScopeFactory<ContinuousPortrayal2D> {

    private static final double SCALE = 0.5;
    private Factory<? super SimulationScope, ? extends VesselField> vesselField;

    @Override
    protected ContinuousPortrayal2D newInstance(final SimulationScope scope) {
        final ContinuousPortrayal2D continuousPortrayal2D = new ContinuousPortrayal2D();
        continuousPortrayal2D.setField(vesselField.get(scope).getField());
        continuousPortrayal2D.setPortrayalForAll(
            new OrientedPortrayal2D(
                new SimplePortrayal2D(),
                0,
                SCALE,
                LIGHT_GRAY,
                SHAPE_COMPASS
            )
        );
        return continuousPortrayal2D;
    }
}
