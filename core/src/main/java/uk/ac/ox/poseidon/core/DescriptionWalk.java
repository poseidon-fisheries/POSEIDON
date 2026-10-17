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

import static java.beans.Introspector.getBeanInfo;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ox.poseidon.core.DescriptionCheck.ReachedNode;

/**
 * Walks a scenario's description from its components, meeting each node once
 * however many ways it is reached, and runs the {@link DescriptionCheck}s it
 * meets over every node.
 */
final class DescriptionWalk {

  private static final ClassValue<List<PropertyDescriptor>> PROPERTIES =
    new ClassValue<>() {
      @Override
      protected List<PropertyDescriptor> computeValue(final Class<?> type) {
        try {
          return Arrays.stream(
            getBeanInfo(type, Object.class).getPropertyDescriptors()
          )
            .filter(property -> property.getReadMethod() != null)
            .toList();
        } catch (final IntrospectionException e) {
          throw new IllegalStateException(e);
        }
      }
    };

  private final List<ReachedNode> nodes = new ArrayList<>();
  private final Set<Object> met = Collections.newSetFromMap(
    new IdentityHashMap<>()
  );

  private DescriptionWalk() {}

  /**
   * @param scenario the scenario whose description to check
   * @throws IllegalStateException if a check fails
   */
  static void check(final Scenario scenario) {
    final DescriptionWalk walk = new DescriptionWalk();
    scenario
      .getComponents()
      .values()
      .forEach(component -> walk.visit(component, null));
    final List<ReachedNode> nodes = List.copyOf(walk.nodes);
    nodes
      .stream()
      .map(ReachedNode::node)
      .filter(DescriptionCheck.class::isInstance)
      .forEach(node -> ((DescriptionCheck) node).check(nodes));
  }

  private void visit(final Object value, final Object parent) {
    switch (value) {
      case final Map<?, ?> map -> map
        .values()
        .forEach(element -> visit(element, parent));
      case final Collection<?> collection -> collection.forEach(element ->
        visit(element, parent)
      );
      case final Factory<?, ?> node -> visitNode(node, parent);
      case final DescriptionNode node -> visitNode(node, parent);
      case null, default -> {
      }
    }
  }

  private void visitNode(final Object node, final Object parent) {
    if (met.add(node)) {
      nodes.add(new ReachedNode(node, parent));
      for (final PropertyDescriptor property : PROPERTIES.get(
        node.getClass()
      )) {
        visit(read(node, property), node);
      }
    }
  }

  private static Object read(
    final Object node,
    final PropertyDescriptor property
  ) {
    try {
      return property.getReadMethod().invoke(node);
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
        "Cannot read " + property.getName() + " on " + node.getClass(),
        e
      );
    }
  }
}
