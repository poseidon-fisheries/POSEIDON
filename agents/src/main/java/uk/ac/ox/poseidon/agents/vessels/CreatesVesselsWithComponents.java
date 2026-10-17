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

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import uk.ac.ox.poseidon.core.ScenarioCheck;

/**
 * What creates vessels, and lists the declarations of the components they are
 * created with. It checks, before a simulation starts, that every declaration
 * the description uses is one some vessel creator lists: a consumer reading a
 * component no vessel has is a wiring error, caught before the first step
 * rather than when the consumer is first used.
 */
interface CreatesVesselsWithComponents extends ScenarioCheck {
  /** @return the declarations of the components its vessels are created with */
  Stream<VesselComponent<?>> listedComponents();

  /**
   * @throws IllegalStateException naming the consumer of a declaration that
   *                               no vessel creator in the description lists
   */
  @Override
  default void check(final List<ReachedNode> nodes) {
    final Set<Object> listed = Collections.newSetFromMap(
      new IdentityHashMap<>()
    );
    nodes
      .stream()
      .map(ReachedNode::node)
      .filter(CreatesVesselsWithComponents.class::isInstance)
      .flatMap(node -> ((CreatesVesselsWithComponents) node).listedComponents())
      .forEach(listed::add);
    nodes
      .stream()
      .filter(reached -> reached.node() instanceof VesselComponent<?>)
      .filter(reached -> !listed.contains(reached.node()))
      .findFirst()
      .ifPresent(reached -> {
        throw new IllegalStateException(
          "A vessel component declaration used by " +
            (reached.parent() == null
              ? "the scenario"
              : reached.parent().getClass().getName()) +
            " is listed by no vessel creator"
        );
      });
  }
}
