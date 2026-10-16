/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.core;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.ac.ox.poseidon.core.schedule.Factories.scheduledOnceAtStart;

import org.junit.jupiter.api.Test;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * Only the build of a scheduling factory receives a scope that schedules: the
 * inputs it asks for receive a plain simulation scope.
 */
class SchedulingFactoryTest {

  private final Simulation simulation = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .build()
    .startNewSimulation();
  private final SimulationScope scope = new SimulationScope(simulation);

  @Test
  void aSchedulingFactorysBuildReceivesASchedulingScope() {
    assertThat(new ScopeClassFactory().get(scope)).isEqualTo(
      SchedulingScope.class
    );
  }

  @Test
  void anInputBuiltUnderAWrapperReceivesAPlainSimulationScope() {
    final StepFactory step = new StepFactory();

    scheduledOnceAtStart(step).get(scope);

    assertThat(step.get(scope).scopeClass()).isEqualTo(SimulationScope.class);
  }

  /** A step that does nothing, built knowing the class of its build's scope. */
  record Step(Class<? extends Scope> scopeClass) implements Steppable {
    @Override
    public void step(final SimState simState) {}
  }

  static final class StepFactory extends SimulationScopeFactory<Step> {

    @Override
    protected Step newInstance(final SimulationScope scope) {
      return new Step(scope.getClass());
    }
  }

  static final class ScopeClassFactory
    extends SchedulingFactory<Class<? extends Scope>>
  {

    @Override
    protected Class<? extends Scope> newInstance(final SchedulingScope scope) {
      return scope.getClass();
    }
  }
}
