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

import sim.display.Controller;
import sim.display.GUIState;
import uk.ac.ox.poseidon.core.Simulation;

import javax.swing.*;

public abstract class DisplayWrapper<D> {

    private final String title;
    D display;
    private JFrame displayFrame;

    DisplayWrapper(
        final String title
    ) {
        this.title = title;
    }

    void init(
        final Controller controller,
        final GUIState guiState
    ) {
        display = createDisplay(guiState);
        displayFrame = createFrame();
        displayFrame.setTitle(title);
        controller.registerFrame(displayFrame);
        displayFrame.setVisible(true);
    }

    void quit() {
        if (displayFrame != null) displayFrame.dispose();
        displayFrame = null;
        display = null;
    }

    abstract void setupPortrayals(Simulation simulation);

    abstract D createDisplay(GUIState guiState);

    abstract JFrame createFrame();

}
