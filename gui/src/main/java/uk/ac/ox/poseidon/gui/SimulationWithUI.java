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

import com.formdev.flatlaf.FlatLightLaf;
import com.google.common.collect.ImmutableList;
import sim.display.Controller;
import sim.display.GUIState;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.List;
import java.util.function.Supplier;

public class SimulationWithUI extends GUIState {

    private final ImmutableList<DisplayWrapper<?, ?>> displayWrappers;
    private final Supplier<Simulation> simulationSupplier;

    public SimulationWithUI(
        final Supplier<Simulation> simulationSupplier,
        final List<DisplayWrapper<?, ?>> displayWrappers
    ) {
        this(simulationSupplier, ImmutableList.copyOf(displayWrappers));
    }
    public SimulationWithUI(
        final Supplier<Simulation> simulationSupplier,
        final ImmutableList<DisplayWrapper<?, ?>> displayWrappers
    ) {
        super(simulationSupplier.get());
        this.simulationSupplier = simulationSupplier;
        this.displayWrappers = displayWrappers;
        FlatLightLaf.setup();
    }

    public static String getName() {
        return "POSEIDON";
    }

    @Override
    public void start() {
        final Simulation simulation = simulationSupplier.get();
        super.state = simulation;
        super.start();
        displayWrappers.forEach(displayWrapper -> displayWrapper.setupPortrayals(simulation));
    }

    @Override
    public void init(final Controller controller) {
        super.init(controller);
        displayWrappers.forEach(displayWrapper -> displayWrapper.init(controller, this));
    }

    @Override
    public Object getSimulationInspectedObject() {
        return state;
    }

    @Override
    public void quit() {
        super.quit();
        displayWrappers.forEach(DisplayWrapper::quit);
    }
}
