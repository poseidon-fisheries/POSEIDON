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

/**
 * Wires a {@link uk.ac.ox.poseidon.core.Scenario} into a MASON GUI:
 * {@link uk.ac.ox.poseidon.gui.ScenarioWithUI} builds a
 * {@link uk.ac.ox.poseidon.gui.SimulationWithUI} ({@link sim.display.GUIState}), which in turn
 * drives one {@link uk.ac.ox.poseidon.gui.DisplayWrapper} per display window (see
 * {@link uk.ac.ox.poseidon.gui.DisplayWrapper2D} for the 2D case). See
 * {@link uk.ac.ox.poseidon.gui.portrayals} for the factories that build the portrayals a display
 * attaches, and {@link uk.ac.ox.poseidon.gui.palettes} for the colour maps they use.
 */
package uk.ac.ox.poseidon.gui;
