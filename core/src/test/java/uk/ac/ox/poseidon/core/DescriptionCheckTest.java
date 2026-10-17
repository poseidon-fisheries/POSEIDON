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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.DescriptionCheck.ReachedNode;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * The checks in a description run before a simulation of it starts, over
 * every node of the description, each met once with the node it was first
 * reached from.
 */
class DescriptionCheckTest {

  @Test
  void aCheckSeesEveryNodeOnceWithWhereItWasFirstReachedFrom() {
    final Leaf shared = new Leaf();
    final RecordingCheck check = new RecordingCheck(List.of(shared, shared));
    scenarioWith(check).startNewSimulation(0).finish();

    assertThat(check.seen)
      .extracting(ReachedNode::node)
      .containsExactly(check, shared);
    assertThat(check.seen)
      .extracting(ReachedNode::parent)
      .containsExactly(null, check);
  }

  @Test
  void aFailedCheckStopsTheSimulationBeforeAnythingIsBuilt() {
    final Leaf leaf = new Leaf();
    final RecordingCheck failing = new RecordingCheck(List.of(leaf)) {
      @Override
      public void check(final List<ReachedNode> nodes) {
        throw new IllegalStateException("wrong wiring");
      }
    };

    assertThatThrownBy(() -> scenarioWith(failing).startNewSimulation(0))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("wrong wiring");
    assertThat(leaf.built).isFalse();
  }

  private static Scenario scenarioWith(final Factory<Scope, ?> component) {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .component("check", component)
      .build();
  }

  static final class Leaf extends GlobalScopeFactory<Object> {

    private boolean built;

    @Override
    protected Object newInstance(final Scope scope) {
      built = true;
      return new Object();
    }
  }

  /** Holds its children, builds them, and records what it sees. */
  static class RecordingCheck
    extends GlobalScopeFactory<Object>
    implements DescriptionCheck
  {

    private final List<Leaf> children;
    private final List<ReachedNode> seen = new ArrayList<>();

    RecordingCheck(final List<Leaf> children) {
      this.children = children;
    }

    public List<Leaf> getChildren() {
      return children;
    }

    @Override
    public void check(final List<ReachedNode> nodes) {
      seen.addAll(nodes);
    }

    @Override
    protected Object newInstance(final Scope scope) {
      children.forEach(child -> child.get(scope));
      return new Object();
    }
  }
}
