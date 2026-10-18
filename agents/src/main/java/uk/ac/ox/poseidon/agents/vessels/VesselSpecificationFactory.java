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

import static com.google.common.base.Preconditions.checkNotNull;

import com.badlogic.gdx.ai.btree.Task;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ScenarioCheck;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.SimulationScopeFactory;

/**
 * A {@link SimulationScopeFactory} counterpart of {@link VesselSpecification},
 * built via {@link Factories#vesselSpecification}.
 * <p>
 * It checks, before a simulation starts, that every declaration the
 * description uses is one it lists: a consumer reading a component no vessel
 * has is a wiring error, caught before the first step rather than when the
 * consumer is first used.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class VesselSpecificationFactory
  extends SimulationScopeFactory<VesselSpecification>
  implements ScenarioCheck
{

  private final Factory<? super SimulationScope, ? extends Fleet> fleet;
  private final VesselComponent<? extends Task<Vessel>> behaviour;

  @Singular
  private final ImmutableMap<String, VesselComponent<?>> components;

  private final String homePortAttribute;
  private final String nameAttribute;

  /**
   * @return the behaviour's declaration, then the other components', in the
   *         order they are built
   */
  private Stream<VesselComponent<?>> listedComponents() {
    return Stream.concat(Stream.of(behaviour), components.values().stream());
  }

  /**
   * @throws IllegalStateException naming the consumer of a declaration that
   *                               no vessel specification in the description
   *                               lists
   */
  @Override
  public void check(final List<ReachedNode> nodes) {
    final Set<Object> listed = Collections.newSetFromMap(
      new IdentityHashMap<>()
    );
    nodes
      .stream()
      .map(ReachedNode::node)
      .filter(VesselSpecificationFactory.class::isInstance)
      .flatMap(node -> ((VesselSpecificationFactory) node).listedComponents())
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
            " is not in the vessel specification"
        );
      });
  }

  @Override
  protected VesselSpecification newInstance(final SimulationScope scope) {
    return new VesselSpecification(
      scope,
      fleet.get(scope),
      checkNotNull(behaviour),
      components,
      checkNotNull(homePortAttribute),
      nameAttribute
    );
  }
}
