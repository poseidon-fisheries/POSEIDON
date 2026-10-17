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

package uk.ac.ox.poseidon.agents.vessels;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselComponent;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.agents.market.MarketGrid;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.SimpleEventManager;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A vessel keeps each component under the declaration it is built from, with
 * a store of its own; a declaration the vessel was not created with is a
 * wiring error, not an absence.
 */
class VesselComponentTest {

  private final Vessel vessel = new Vessel(
    mock(TemporalSchedule.class),
    new SimpleEventManager(),
    "V1",
    mock(VesselField.class),
    mock(PortGrid.class),
    mock(MarketGrid.class)
  );
  private final VesselScope scope = new VesselScope(
    mock(SimulationScope.class),
    vessel
  );

  @Test
  void theVesselHasTheComponentItsDeclarationBuilt() {
    final VesselComponent<Object> declaration = vesselComponent(
      new NewObjectFactory()
    );

    final Object component = vessel.buildComponent(declaration, scope);

    assertThat(declaration.of(vessel)).isSameAs(component);
    assertThat(declaration.findIn(vessel)).containsSame(component);
  }

  @Test
  void buildingAComponentAgainReusesItsStore() {
    final VesselComponent<Object> declaration = vesselComponent(
      new NewObjectFactory()
    );

    assertThat(vessel.buildComponent(declaration, scope)).isSameAs(
      vessel.buildComponent(declaration, scope)
    );
  }

  @Test
  void twoDeclarationsOfOneFactoryGiveTwoComponents() {
    final NewObjectFactory factory = new NewObjectFactory();
    final VesselComponent<Object> first = vesselComponent(factory);
    final VesselComponent<Object> second = vesselComponent(factory);
    vessel.buildComponent(first, scope);
    vessel.buildComponent(second, scope);

    assertThat(first.of(vessel)).isNotSameAs(second.of(vessel));
  }

  @Test
  void aDeclarationTheVesselWasNotCreatedWithIsAWiringError() {
    final VesselComponent<Object> declaration = vesselComponent(
      new NewObjectFactory()
    );

    assertThatThrownBy(() -> declaration.of(vessel))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1");
    assertThatThrownBy(() -> declaration.findIn(vessel)).isInstanceOf(
      IllegalStateException.class
    );
  }

  private static final class NewObjectFactory
    extends VesselComponentScopeFactory<Object>
  {

    @Override
    protected Object newInstance(final VesselComponentScope scope) {
      return new Object();
    }
  }
}
