/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

import static uk.ac.ox.poseidon.core.scopes.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;

import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.*;
import uk.ac.ox.poseidon.core.schedule.TemporalSchedule;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;

/**
 * An immutable-by-convention (Lombok {@link Builder}), declarative bag of named
 * component {@link Factory} instances plus a starting date/time — the
 * serializable, YAML-loadable description of a simulation to run. Every field
 * must remain serializable to YAML; don't introduce fields/types that can't
 * survive a SnakeYAML round-trip. {@link #startNewSimulation()} is the entry
 * point: it resolves every component factory against a fresh
 * {@link SimulationScope} to build a runnable {@link Simulation}. A scenario
 * that differs in some value is derived from this one by {@link #with},
 * {@link #withComponent} or {@link #withStartingDateTime}, which leave this
 * one untouched, so simulations of several derived scenarios can be built at
 * the same time.
 */
@Getter
@Builder(toBuilder = true)
public final class Scenario {

  /** The simulation's starting date-time. */
  private final Factory<Scope, LocalDateTime> startingDateTime;

  /**
   * The named component factories making up the scenario. A name contains
   * none of the characters {@code .()[]}, which separate the steps of an address
   * given to {@link #with}.
   */
  @Singular
  private final Map<
    String,
    ? extends Factory<? super SimulationScope, ?>
  > components;

  /**
   * @param startingDateTime the simulation's starting date-time
   * @param components       the named component factories making up the
   *                         scenario
   * @throws IllegalArgumentException if a component's name contains one of
   *                                  the characters {@code .()[]}
   */
  public Scenario(
    final Factory<Scope, LocalDateTime> startingDateTime,
    final Map<String, ? extends Factory<? super SimulationScope, ?>> components
  ) {
    components
      .keySet()
      .stream()
      .filter(name -> name.chars().anyMatch(c -> ".()[]".indexOf(c) >= 0))
      .findFirst()
      .ifPresent(name -> {
        throw new IllegalArgumentException(
          "A component's name cannot contain any of .()[]: " + name
        );
      });
    this.startingDateTime = startingDateTime;
    this.components = ImmutableMap.copyOf(components);
  }

  /**
   * @param startingDateTime the simulation's starting date-time
   * @param components       the named component factories making up the
   *                         scenario
   */
  public Scenario(
    final LocalDateTime startingDateTime,
    final Map<String, ? extends Factory<? super SimulationScope, ?>> components
  ) {
    this(dateTime(startingDateTime), components);
  }

  /**
   * @param startingDate the simulation's starting date, at midnight
   * @param components   the named component factories making up the scenario
   */
  public Scenario(
    final LocalDate startingDate,
    final Map<String, ? extends Factory<? super SimulationScope, ?>> components
  ) {
    this(startingDate.atStartOfDay(), components);
  }

  /**
   * Derives a scenario in which the value at {@code address} is {@code value}.
   * The address starts at a component's name and goes on with commons-beanutils
   * syntax: {@code .property}, {@code (key)} into a map, {@code [index]} into
   * a list; {@code gear.proportion}, for instance. A factory, map or list
   * found there is replaced wherever it appears in the scenario; any other
   * value is replaced in the factory, map or list the address ends in. Every
   * factory above a replacement is copied, and every other factory is kept,
   * so the products of global factories it keeps are shared with this
   * scenario's simulations.
   *
   * @param address the address of the value to replace
   * @param value   the new value
   * @return the derived scenario
   * @throws IllegalArgumentException if the address does not lead to a value
   */
  public Scenario with(final String address, final Object value) {
    return ScenarioDerivation.apply(this, address, value);
  }

  /**
   * @param name    the new component's name
   * @param factory the new component's factory
   * @return a scenario with this one's components and the new one, last
   * @throws IllegalArgumentException if a component already has that name, or
   *                                  the name contains one of the characters
   *                                  {@code .()[]}
   */
  public Scenario withComponent(
    final String name,
    final Factory<? super SimulationScope, ?> factory
  ) {
    return toBuilder().component(name, factory).build();
  }

  /**
   * @param startingDateTime the derived scenario's starting date-time
   * @return a scenario with this one's components, starting at
   *         {@code startingDateTime}
   */
  public Scenario withStartingDateTime(final LocalDateTime startingDateTime) {
    return toBuilder().startingDateTime(dateTime(startingDateTime)).build();
  }

  /**
   * Equivalent to {@link #startNewSimulation(long)} with the current time in
   * milliseconds as the seed.
   *
   * @return the started {@link Simulation}
   */
  public Simulation startNewSimulation() {
    return startNewSimulation(System.currentTimeMillis());
  }

  /**
   * Equivalent to {@link #startNewSimulation(long, UUID)} with a fresh random
   * id.
   *
   * @param seed the run's RNG seed
   * @return the started {@link Simulation}
   */
  public Simulation startNewSimulation(final long seed) {
    return startNewSimulation(seed, UUID.randomUUID());
  }

  /**
   * Builds and starts a new {@link Simulation} by resolving every component
   * factory, in order, against a fresh {@link SimulationScope}.
   *
   * @param seed         the run's RNG seed
   * @param simulationId the run's id
   * @return the started {@link Simulation}
   */
  public Simulation startNewSimulation(
    final long seed,
    final UUID simulationId
  ) {
    final TemporalSchedule schedule = new TemporalSchedule(
      startingDateTime.get(GLOBAL_SCOPE)
    );
    final Simulation simulation = new Simulation(seed, schedule, simulationId);
    final SimulationScope simulationScope = new SimulationScope(simulation);
    simulation.start();
    final ImmutableMap.Builder<String, Object> resolved =
      ImmutableMap.builder();
    components.forEach((name, factory) ->
      resolved.put(name, factory.get(simulationScope))
    );
    simulation.components = resolved.buildOrThrow();
    return simulation;
  }

  /**
   * @param componentName the component's name, as registered in
   *                      {@link #components}
   * @return the named component's factory, unchecked-cast to a
   *         {@code Factory<SimulationScope, P>}. The type has no wildcards so
   *         that the compiler can infer {@code P}, and the scope of a generic
   *         factory method given the component, from where the component is
   *         passed: with wildcards, callers need explicit type arguments.
   *         Narrowing the factory's scope to {@link SimulationScope} is safe,
   *         since a factory only takes a scope.
   * @throws IllegalArgumentException if no component is registered under that
   *                                  name
   */
  @SuppressWarnings("unchecked")
  public <P> Factory<SimulationScope, P> component(final String componentName) {
    return (Factory<SimulationScope, P>) component(
      componentName,
      Factory.class
    );
  }

  /**
   * @param componentName the component's name, as registered in
   *                      {@link #components}
   * @param factoryClass  the expected factory type
   * @return the named component's factory, cast to {@code factoryClass}
   * @throws IllegalArgumentException if no component is registered under that
   *                                  name, or if it's not an instance of
   *                                  {@code factoryClass}
   */
  public <F extends Factory<?, ?>> F component(
    final String componentName,
    final Class<? extends F> factoryClass
  ) {
    final var factory = components.get(componentName);
    if (factory == null) {
      throw new IllegalArgumentException(
        "Component not found: " + componentName
      );
    }
    if (!factoryClass.isInstance(factory)) {
      throw new IllegalArgumentException(
        "Factory is not of type " + factoryClass.getName()
      );
    }
    return factoryClass.cast(factory);
  }
}
