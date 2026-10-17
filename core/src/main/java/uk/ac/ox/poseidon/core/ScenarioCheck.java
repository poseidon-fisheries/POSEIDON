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

import java.util.List;

/**
 * A part of a scenario's description that checks the whole description it
 * belongs to, before a simulation of it starts: a wiring error found this way
 * fails before anything is built, whichever part of the description it lies
 * in.
 */
public interface ScenarioCheck {
  /**
   * @param nodes every {@link ScenarioNode} of the scenario, factories
   *              included, each once, with the node it was
   *              first reached from, in the order a walk from the scenario's
   *              components meets them
   * @throws IllegalStateException naming what is wrong
   */
  void check(List<ReachedNode> nodes);

  /**
   * @param node   a node of the description
   * @param parent the node {@code node} was first reached from, or
   *               {@code null} for a scenario component
   */
  record ReachedNode(Object node, Object parent) {}
}
