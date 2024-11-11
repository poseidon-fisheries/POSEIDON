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

package uk.ac.ox.poseidon.gui;

import sim.display.Display2D;
import sim.display.GUIState;
import sim.portrayal.FieldPortrayal2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

public class DisplayWrapper2D extends DisplayWrapper<Display2D, FieldPortrayal2D> {

    private static final int DEFAULT_STEP_INTERVAL = 64;

    private final double width;
    private final double height;
    private final Paint backDrop;

    public DisplayWrapper2D(
        final String title,
        final Map<String, Factory<? extends FieldPortrayal2D>> portrayalFactories,
        final double width,
        final double height,
        final Paint backDrop
    ) {
        super(title, portrayalFactories);
        this.width = width;
        this.height = height;
        this.backDrop = backDrop;
    }

    @Override
    Display2D createDisplay(final GUIState guiState) {
        return new Display2D(width, height, guiState) {
            {
                setClipping(false);
                updateRule = UPDATE_RULE_STEPS;
                stepInterval = DEFAULT_STEP_INTERVAL;
            }
        };
    }

    @Override
    JFrame createFrame() {
        return display.createFrame();
    }

    @Override
    void setupPortrayals(final Simulation simulation) {
        display.detachAll();
        portrayalFactories.forEach((name, factory) ->
            display.attach(factory.get(simulation), name)
        );
        display.reset();
        display.setBackdrop(backDrop);
        display.repaint();
    }

}
