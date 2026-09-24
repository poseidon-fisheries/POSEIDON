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

package uk.ac.ox.poseidon.gui;

import com.formdev.flatlaf.FlatLightLaf;
import com.google.common.collect.ImmutableList;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import sim.display.Controller;
import sim.display.GUIState;
import sim.engine.Steppable;
import sim.portrayal.Inspector;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;

import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The MASON {@link GUIState} for a POSEIDON {@link Simulation}: on each (re)start it builds a
 * fresh simulation from {@code simulationSupplier} and hands it to every registered
 * {@link DisplayWrapper} to set up portrayals for.
 */
public class SimulationWithUI extends GUIState {

    private final ImmutableList<DisplayWrapper<?>> displayWrappers;
    private final Supplier<Simulation> simulationSupplier;
    private Simulation simulation;

    /**
     * @param simulationSupplier builds a fresh {@link Simulation} on each (re)start
     * @param displayWrappers    the displays to set up and tear down alongside the simulation
     */
    public SimulationWithUI(
        final Supplier<Simulation> simulationSupplier,
        final List<DisplayWrapper<?>> displayWrappers
    ) {
        this(simulationSupplier, ImmutableList.copyOf(displayWrappers));
    }

    /**
     * @param simulationSupplier builds a fresh {@link Simulation} on each (re)start
     * @param displayWrappers    the displays to set up and tear down alongside the simulation
     */
    public SimulationWithUI(
        final Supplier<Simulation> simulationSupplier,
        final ImmutableList<DisplayWrapper<?>> displayWrappers
    ) {
        super(simulationSupplier.get());
        this.simulationSupplier = simulationSupplier;
        this.displayWrappers = displayWrappers;
        FlatLightLaf.setup();
    }

    /**
     * Looked up by MASON's console via reflection (not an {@code @Override}, {@link GUIState}
     * exposes no instance method for it) to label the application window.
     *
     * @return {@code "POSEIDON"}
     */
    @SuppressFBWarnings("HSM")
    public static String getName() {
        return "POSEIDON";
    }

    /**
     * Builds a fresh {@link Simulation} from {@link #simulationSupplier} and sets up portrayals
     * on every registered {@link DisplayWrapper} for it.
     */
    @Override
    public void start() {
        this.simulation = simulationSupplier.get();
        super.state = this.simulation;
        displayWrappers.forEach(displayWrapper -> displayWrapper.setupPortrayals(simulation));
    }

    /**
     * Registers every {@link DisplayWrapper}'s frame with {@code controller}.
     */
    @Override
    public void init(final Controller controller) {
        super.init(controller);
        displayWrappers.forEach(displayWrapper -> displayWrapper.init(controller, this));
    }

    /**
     * @return a {@link SimulationProxy} pointing at the current simulation, so the model
     * inspector keeps working across restarts (see {@link SimulationProxy}'s own doc)
     */
    @Override
    public Object getSimulationInspectedObject() {
        return new SimulationProxy();
    }

    /**
     * @return the inherited inspector, marked volatile so it refreshes across simulation restarts
     */
    @Override
    public Inspector getInspector() {
        final Inspector inspector = super.getInspector();
        inspector.setVolatile(true);
        return inspector;
    }

    /**
     * Tears down every registered {@link DisplayWrapper} alongside the inherited cleanup.
     */
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

        /** @return the current simulation's ID, or {@code null} if there is none */
        public UUID getId() {
            return propertyOrNull(Simulation::getId);
        }

        /** @return the current simulation's temporal schedule, or {@code null} if there is none */
        public TemporalSchedule getTemporalSchedule() {
            return propertyOrNull(Simulation::getTemporalSchedule);
        }

        /** @return the current simulation's final processes, or {@code null} if there is none */
        public List<Steppable> getFinalProcess() {
            return propertyOrNull(Simulation::getFinalProcesses);
        }

        /** @return the current simulation's components, or {@code null} if there is none */
        public List<?> getComponents() {
            return propertyOrNull(Simulation::getComponents);
        }

    }

}
