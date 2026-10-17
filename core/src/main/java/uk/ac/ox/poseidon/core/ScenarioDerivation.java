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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.beanutils.expression.DefaultResolver;
import org.apache.commons.beanutils.expression.Resolver;
import uk.ac.ox.poseidon.core.utils.Builders;

/**
 * Derives a scenario in which the value at one address is replaced, leaving the
 * original scenario untouched.
 * <p>
 * An address starts at a component's name and goes on with commons-beanutils
 * syntax: {@code .property}, {@code (key)} into a map, {@code [index]} into a
 * list. Following it only reads. If the value found is a node (a
 * {@link ScenarioNode}, such as a factory, or a map or a list), it is replaced
 * in every
 * container that holds it, since the same node may be reached in several
 * ways. Any other value is replaced in the container the address ends in,
 * which is then the changed node.
 * <p>
 * Every node that holds a changed node, directly or not, up to the scenario,
 * is then copied once, through its builder, with each changed child replaced
 * by its copy. Every other node is kept, so its products, global ones
 * included, stay shared with the original scenario. Maps and lists are copied
 * as Guava immutable collections, in their original order.
 */
final class ScenarioDerivation {

  private static final Resolver RESOLVER = new DefaultResolver();

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

  /** Each node met so far, with what replaces it, itself if unchanged. */
  private final Map<Object, Object> copies = new IdentityHashMap<>();

  private ScenarioDerivation() {}

  /**
   * @param scenario the scenario to derive from
   * @param address  the address of the value to replace
   * @param value    the new value
   * @return a new scenario, with the value at {@code address} replaced
   * @throws IllegalArgumentException if the address does not lead to a value
   */
  static Scenario apply(
    final Scenario scenario,
    final String address,
    final Object value
  ) {
    final List<Step> steps = steps(address);
    Object container = scenario.getComponents();
    for (final Step step : steps.subList(0, steps.size() - 1)) {
      container = step.read(container, address);
    }
    final Step last = steps.getLast();
    final Object target = last.read(container, address);
    final ScenarioDerivation override = new ScenarioDerivation();
    if (isNode(target)) {
      override.copies.put(target, value);
    } else {
      override.copies.put(container, last.write(container, value));
    }
    return (Scenario) override.rebuilt(scenario);
  }

  private static List<Step> steps(final String address) {
    final List<Step> steps = new ArrayList<>();
    for (
      String rest = address;
      rest != null && !rest.isEmpty();
      rest = RESOLVER.remove(rest)
    ) {
      final String expression = RESOLVER.next(rest);
      final String name = RESOLVER.getProperty(expression);
      // The address starts in the scenario's map of components
      steps.add(steps.isEmpty() ? new Key(name) : new Property(name));
      if (RESOLVER.isMapped(expression)) {
        steps.add(new Key(RESOLVER.getKey(expression)));
      }
      if (RESOLVER.isIndexed(expression)) {
        steps.add(new Index(RESOLVER.getIndex(expression)));
      }
    }
    if (steps.isEmpty()) {
      throw new IllegalArgumentException("Empty address");
    }
    return steps;
  }

  private static boolean isNode(final Object object) {
    return (
      object instanceof ScenarioNode ||
      object instanceof Map<?, ?> ||
      object instanceof Collection<?>
    );
  }

  private Object rebuilt(final Object node) {
    if (copies.containsKey(node)) {
      return copies.get(node);
    }
    final Object copy = switch (node) {
      case final Map<?, ?> map -> rebuiltMap(map);
      case final List<?> list -> rebuiltList(list);
      case final Collection<?> collection -> checkedUnchanged(collection);
      case final ScenarioNode scenarioNode -> rebuiltBean(scenarioNode);
      case final Scenario scenario -> rebuiltBean(scenario);
      case null, default -> node;
    };
    copies.put(node, copy);
    return copy;
  }

  private Object rebuiltMap(final Map<?, ?> map) {
    final Map<Object, Object> rebuilt = new LinkedHashMap<>();
    map.forEach((key, value) -> rebuilt.put(key, rebuilt(value)));
    return changed(map.values(), rebuilt.values())
      ? ImmutableMap.copyOf(rebuilt)
      : map;
  }

  private Object rebuiltList(final List<?> list) {
    final List<Object> rebuilt = list.stream().map(this::rebuilt).toList();
    return changed(list, rebuilt) ? ImmutableList.copyOf(rebuilt) : list;
  }

  private Object checkedUnchanged(final Collection<?> collection) {
    if (changed(collection, collection.stream().map(this::rebuilt).toList())) {
      throw new IllegalArgumentException(
        "Cannot copy a " + collection.getClass().getName()
      );
    }
    return collection;
  }

  private static boolean changed(
    final Collection<?> originals,
    final Collection<?> copies
  ) {
    final var copy = copies.iterator();
    return originals.stream().anyMatch(original -> original != copy.next());
  }

  private Object rebuiltBean(final Object bean) {
    final Map<String, Object> changes = new LinkedHashMap<>();
    for (final PropertyDescriptor property : PROPERTIES.get(bean.getClass())) {
      final Object child = read(bean, property);
      final Object copy = rebuilt(child);
      if (copy != child) {
        changes.put(property.getName(), copy);
      }
    }
    return changes.isEmpty() ? bean : copy(bean, changes);
  }

  private static Object read(
    final Object bean,
    final PropertyDescriptor property
  ) {
    try {
      return property.getReadMethod().invoke(bean);
    } catch (final IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
        "Cannot read " + property.getName() + " on " + bean.getClass(),
        e
      );
    }
  }

  private static Object copy(
    final Object bean,
    final Map<String, Object> changes
  ) {
    final Object builder = Builders.invoke(bean, "toBuilder");
    changes.forEach((name, value) -> {
      // A builder made by toBuilder() holds the original's elements, which
      // the method of a collection marked @Singular adds to
      final String clear =
        "clear" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
      if (
        Arrays.stream(builder.getClass().getMethods()).anyMatch(method ->
          method.getName().equals(clear)
        )
      ) {
        Builders.invoke(builder, clear);
      }
      Builders.invoke(builder, name, value);
    });
    return Builders.invoke(builder, "build");
  }

  /** One step along an address: into a bean property, a map key or a list index. */
  private sealed interface Step {
    Object read(Object container, String address);

    Object write(Object container, Object value);
  }

  private record Property(String name) implements Step {
    @Override
    public Object read(final Object container, final String address) {
      if (!(container instanceof ScenarioNode)) {
        throw new IllegalArgumentException(
          "No property " + name + " to follow in " + address
        );
      }
      return PROPERTIES.get(container.getClass())
        .stream()
        .filter(property -> property.getName().equals(name))
        .findFirst()
        .map(property -> ScenarioDerivation.read(container, property))
        .orElseThrow(() ->
          new IllegalArgumentException(
            "No property " +
              name +
              " on " +
              container.getClass().getName() +
              " in " +
              address
          )
        );
    }

    @Override
    public Object write(final Object container, final Object value) {
      final Map<String, Object> changes = new LinkedHashMap<>();
      changes.put(name, value);
      return copy(container, changes);
    }
  }

  private record Key(String key) implements Step {
    @Override
    public Object read(final Object container, final String address) {
      if (!(container instanceof Map<?, ?> map)) {
        throw new IllegalArgumentException(
          "No map to take " + key + " from in " + address
        );
      }
      return map
        .entrySet()
        .stream()
        .filter(entry -> String.valueOf(entry.getKey()).equals(key))
        .findFirst()
        .orElseThrow(() ->
          new IllegalArgumentException("No key " + key + " in " + address)
        )
        .getValue();
    }

    @Override
    public Object write(final Object container, final Object value) {
      final Map<Object, Object> written = new LinkedHashMap<>(
        (Map<?, ?>) container
      );
      written.replaceAll((entryKey, entryValue) ->
        String.valueOf(entryKey).equals(key) ? value : entryValue
      );
      return ImmutableMap.copyOf(written);
    }
  }

  private record Index(int index) implements Step {
    @Override
    public Object read(final Object container, final String address) {
      if (
        !(container instanceof List<?> list) ||
        index < 0 ||
        index >= list.size()
      ) {
        throw new IllegalArgumentException(
          "No element " + index + " in " + address
        );
      }
      return list.get(index);
    }

    @Override
    public Object write(final Object container, final Object value) {
      final List<Object> written = new ArrayList<>((List<?>) container);
      written.set(index, value);
      return ImmutableList.copyOf(written);
    }
  }
}
