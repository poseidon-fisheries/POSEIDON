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
import sim.engine.Steppable;
import sim.portrayal.Inspector;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class SimulationWithUI extends GUIState {

    private final ImmutableList<DisplayWrapper<?, ?>> displayWrappers;
    private final Supplier<Simulation> simulationSupplier;
    private Simulation simulation;

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
        this.simulation = simulationSupplier.get();
        super.state = this.simulation;
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
        return new SimulationProxy();
    }

    @Override
    public Inspector getInspector() {
        final Inspector inspector = super.getInspector();
        inspector.setVolatile(true);
        return inspector;
    }

    @Override
    public void quit() {
        super.quit();
        displayWrappers.forEach(DisplayWrapper::quit);
    }

    /**
     * This class is there to get around the fact that the MASON model inspector is tied to a
     * particular object that cannot be changed without reconstructing the inspector. Since we build
     * a new simulation object everytime it is restarted (instead of re-initialising the same object
     * as is more common in MASON), we use this proxy class pointing to the current simulation for
     * the inspector to display.
     */
    @SuppressWarnings("WeakerAccess")
    public class SimulationProxy {

        private <T> T propertyOrNull(final Function<Simulation, T> property) {
            return SimulationWithUI.this.simulation != null
                ? property.apply(SimulationWithUI.this.simulation)
                : null;
        }

        public Scenario getScenario() {
            return propertyOrNull(Simulation::getScenario);
        }

        public Long getId() {
            return propertyOrNull(Simulation::getId);
        }

        public TemporalSchedule getTemporalSchedule() {
            return propertyOrNull(Simulation::getTemporalSchedule);
        }

        public List<Steppable> getFinalProcess() {
            return propertyOrNull(Simulation::getFinalProcesses);
        }

        public List<?> getComponents() {
            return propertyOrNull(Simulation::getComponents);
        }

    }

}
