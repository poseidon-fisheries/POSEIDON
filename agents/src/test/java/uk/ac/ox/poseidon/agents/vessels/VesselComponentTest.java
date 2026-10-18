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
import static uk.ac.ox.poseidon.agents.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.agents.vessels.Factories.by;
import static uk.ac.ox.poseidon.agents.vessels.Factories.choice;
import static uk.ac.ox.poseidon.agents.vessels.Factories.none;
import static uk.ac.ox.poseidon.agents.vessels.Factories.otherwise;
import static uk.ac.ox.poseidon.agents.vessels.Factories.vesselComponent;
import static uk.ac.ox.poseidon.agents.vessels.Factories.when;
import static uk.ac.ox.poseidon.agents.vessels.SpecificationFixtures.newVessel;

import com.badlogic.gdx.ai.btree.leaf.Success;
import java.util.Map;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.fields.VesselField;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A vessel keeps each component under the declaration it is built from, with
 * a store of its own; a declaration the vessel was not created with is a
 * wiring error, not an absence.
 */
class VesselComponentTest {

  private final VesselComponent<Object> declaration = vesselComponent(
    new NewObjectFactory()
  );

  /** @return a vessel created with {@code components}, all built */
  private static Vessel vesselWith(final VesselComponent<?>... components) {
    final Vessel vessel = newVessel(
      mock(TemporalSchedule.class),
      mock(VesselField.class),
      mock(PortGrid.class),
      behaviour(scope -> new Success<>()),
      components
    );
    vessel.buildComponents();
    return vessel;
  }

  @Test
  void theVesselHasTheComponentItsDeclarationBuilt() {
    final Vessel vessel = vesselWith(declaration);

    assertThat(declaration.of(vessel)).isInstanceOf(Object.class);
    assertThat(declaration.findIn(vessel)).containsSame(declaration.of(vessel));
  }

  @Test
  void twoDeclarationsOfOneFactoryAreTwoKeysAndGiveTwoComponents() {
    final NewObjectFactory factory = new NewObjectFactory();
    final VesselComponent<Object> first = vesselComponent(factory);
    final VesselComponent<Object> second = vesselComponent(factory);
    final Vessel vessel = vesselWith(first, second);

    assertThat(first).isNotEqualTo(second);
    assertThat(first.of(vessel)).isNotSameAs(second.of(vessel));
  }

  @Test
  void aDeclarationTheVesselWasNotCreatedWithIsAWiringError() {
    final Vessel vessel = vesselWith();

    assertThatThrownBy(() -> declaration.of(vessel))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Vessel vessel");
    assertThatThrownBy(() -> declaration.findIn(vessel)).isInstanceOf(
      IllegalStateException.class
    );
  }

  @Test
  void aChoiceOfNoComponentLeavesTheComponentAbsent() {
    final VesselComponent<Object> chosen = choice(
      by("gear"),
      when("PS").then(new NewObjectFactory()),
      otherwise(none())
    );
    final Vessel vessel = newVessel(
      mock(TemporalSchedule.class),
      mock(VesselField.class),
      mock(PortGrid.class),
      behaviour(scope -> new Success<>()),
      chosen
    );
    vessel.setAttributes(Map.of("gear", "GNS"));
    vessel.buildComponents();

    assertThat(chosen.findIn(vessel)).isEmpty();
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
