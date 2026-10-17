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
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/**
 * A factory's level is declared by its base class, or derived by a relative
 * factory from the inputs typed in its scope variable, ignoring the children
 * it holds. A product is asked for with a scope at least as narrow as its
 * factory's level, and its build receives a scope of exactly that level.
 */
class LevelsTest {

  private final Simulation first = newSimulation();
  private final Simulation second = newSimulation();
  private final SimulationScope firstScope = new SimulationScope(first);
  private final SimulationScope secondScope = new SimulationScope(second);

  private static Simulation newSimulation() {
    return Scenario.builder()
      .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
      .build()
      .startNewSimulation();
  }

  @AfterEach
  void finish() {
    first.finish();
    second.finish();
  }

  // The compiler refuses a wider scope; a description wired without types,
  // as YAML is, reaches the factory raw.
  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  void aProductAskedForWithAWiderScopeIsRefused() {
    final Factory factory = new SimulationProductFactory();

    assertThatThrownBy(() -> factory.get(GLOBAL_SCOPE))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining(SimulationProductFactory.class.getName());
  }

  @Test
  void aBuildReceivesAScopeOfExactlyItsLevel() {
    assertThat(
      new SimulationProductFactory().get(new SchedulingScope(firstScope))
    ).isEqualTo(new Product(SimulationScope.class));
  }

  @Test
  void aRelativeFactoryWithNoInputIsGlobal() {
    final RelativeProductFactory<Scope> factory = new RelativeProductFactory<>(
      null,
      null
    );

    assertThat(factory.get(firstScope))
      .isSameAs(factory.get(secondScope))
      .isEqualTo(new Product(Scope.class));
  }

  @Test
  void aRelativeFactoryTakesTheLevelOfItsNarrowestInput() {
    final RelativeProductFactory<SimulationScope> factory =
      new RelativeProductFactory<>(
        new RelativeProductFactory<>(new SimulationProductFactory(), null),
        null
      );

    assertThat(factory.get(firstScope))
      .isSameAs(factory.get(firstScope))
      .isNotSameAs(factory.get(secondScope))
      .isEqualTo(new Product(SimulationScope.class));
  }

  @Test
  void aRelativeFactoryIgnoresTheLevelOfAChildItHolds() {
    final RelativeProductFactory<Scope> factory = new RelativeProductFactory<>(
      new GlobalProductFactory(),
      new SimulationProductFactory()
    );

    assertThat(factory.get(firstScope)).isSameAs(factory.get(secondScope));
  }

  @Test
  void aRelativeFactoryWithAnInputOfNoReadableLevelTakesTheLevelOfItsScope() {
    final RelativeProductFactory<SimulationScope> factory =
      new RelativeProductFactory<>(
        scope -> new Product(scope.getClass()),
        null
      );

    assertThat(factory.get(firstScope)).isNotSameAs(factory.get(secondScope));
  }

  @Test
  void aChildTypedWithNoScopeIsRefusedNamingItsClassAndProperty() {
    assertThatThrownBy(() -> new UntypedChildFactory().get(firstScope))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(UntypedChildFactory.class.getName())
      .hasMessageContaining("child");
  }

  /** Says the class of the scope its build received. */
  record Product(Class<? extends Scope> scopeClass) {}

  static final class GlobalProductFactory extends GlobalScopeFactory<Product> {

    @Override
    protected Product newInstance(final Scope scope) {
      return new Product(scope.getClass());
    }
  }

  static final class SimulationProductFactory
    extends SimulationScopeFactory<Product>
  {

    @Override
    protected Product newInstance(final SimulationScope scope) {
      return new Product(scope.getClass());
    }
  }

  /** Builds its input, if any, and never its held child. */
  static final class RelativeProductFactory<S extends Scope> extends RelativeScopeFactory<
    S,
    Product
  > {

    private final Factory<? super S, ?> input;
    private final Factory<? super SimulationScope, ?> held;

    RelativeProductFactory(
      final Factory<? super S, ?> input,
      final Factory<? super SimulationScope, ?> held
    ) {
      this.input = input;
      this.held = held;
    }

    public Factory<? super S, ?> getInput() {
      return input;
    }

    public Factory<? super SimulationScope, ?> getHeld() {
      return held;
    }

    @Override
    protected Product newInstance(final S scope) {
      if (input != null) {
        input.get(scope);
      }
      return new Product(scope.getClass());
    }
  }

  /** Holds a child whose type cannot tell an input from a held child. */
  static final class UntypedChildFactory
    extends RelativeScopeFactory<Scope, Product>
  {

    private final Factory<?, ?> child = new GlobalProductFactory();

    public Factory<?, ?> getChild() {
      return child;
    }

    @Override
    protected Product newInstance(final Scope scope) {
      return new Product(scope.getClass());
    }
  }
}
