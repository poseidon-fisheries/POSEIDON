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

import com.google.common.collect.ImmutableList;
import sim.display.Display2D;
import sim.display.GUIState;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.gui.portrayals.NamedPortrayal;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.lang.System.Logger.Level.WARNING;

public class DisplayWrapper2D extends DisplayWrapper<Display2D> {

    private static final System.Logger logger = System.getLogger(DisplayWrapper2D.class.getName());

    private static final int DEFAULT_STEP_INTERVAL = 64;

    private final List<Factory<?>> fieldPortrayalFactories;
    private final double width;
    private final double height;
    private final Paint backDrop;

    public DisplayWrapper2D(
        final String title,
        final List<Factory<?>> fieldPortrayalFactories,
        final double width,
        final double height,
        final Paint backDrop
    ) {
        super(title);
        this.fieldPortrayalFactories = ImmutableList.copyOf(fieldPortrayalFactories);
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
        fieldPortrayalFactories
            .stream()
            .flatMap(factory -> {
                final Object o = factory.get(simulation);
                return factory.get(simulation) instanceof final Collection<?> os
                    ? os.stream()
                    : Stream.of(o);
            })
            .forEach(o -> {
                if (o instanceof final NamedPortrayal namedPortrayal) {
                    display.attach(
                        namedPortrayal.getPortrayal(),
                        namedPortrayal.getName(),
                        namedPortrayal.isVisible()
                    );
                } else {
                    logger.log(
                        WARNING,
                        "Expected named portrayal but got {0}",
                        o.getClass().getName()
                    );
                }
            });
        display.reset();
        display.setBackdrop(backDrop);
        display.repaint();
    }

}
