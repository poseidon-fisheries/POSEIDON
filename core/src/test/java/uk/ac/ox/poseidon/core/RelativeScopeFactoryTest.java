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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * A relative factory reads its inputs from the bean properties typed in its
 * own scope variable, whether held directly, in a collection or in a map, and
 * refuses factory-typed properties whose scope argument says neither input
 * nor held child.
 */
class RelativeScopeFactoryTest {

  @Test
  void aCollectionOfFactoriesInTheScopeVariableIsAnInput() {
    assertThat(
      inputReadMethodNamesOf(CollectionInputFactory.class)
    ).containsExactly("getInputs");
  }

  @Test
  void aMapOfFactoriesInTheScopeVariableIsAnInput() {
    assertThat(inputReadMethodNamesOf(MapInputFactory.class)).containsExactly(
      "getInputs"
    );
  }

  @Test
  void aCollectionOfFactoriesWithAConcreteScopeIsHeld() {
    assertThat(inputReadMethodNamesOf(CollectionHeldFactory.class)).isEmpty();
  }

  @Test
  void aPropertyThatIsNotAFactoryIsNotAnInput() {
    assertThat(inputReadMethodNamesOf(PlainPropertyFactory.class)).isEmpty();
  }

  @Test
  void aDirectFactoryInTheScopeVariableIsAnInput() {
    assertThat(
      inputReadMethodNamesOf(DirectInputFactory.class)
    ).containsExactly("getInput");
  }

  @Test
  void aDirectFactoryWithAConcreteScopeIsHeld() {
    assertThat(inputReadMethodNamesOf(DirectHeldFactory.class)).isEmpty();
  }

  @Test
  void aCollectionWithAnUntypedFactoryElementIsRefused() {
    assertThatThrownBy(() ->
      RelativeScopeFactory.inputReadMethodsOf(CollectionUntypedFactory.class)
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining(CollectionUntypedFactory.class.getName())
      .hasMessageContaining("inputs");
  }

  @Test
  @SuppressWarnings({ "rawtypes", "unchecked" })
  void inputsAtLevelsNeitherOfWhichIsNarrowerAreRefused() {
    final RelativeScopeFactory factory = new TwoScopesFactory(
      new LeftLevelFactory() {},
      new RightLevelFactory() {}
    );

    assertThatThrownBy(() -> factory.levelFor(Scope.GLOBAL_SCOPE))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("neither of which is narrower");
  }

  private static List<String> inputReadMethodNamesOf(
    final Class<? extends RelativeScopeFactory<?, ?>> factoryClass
  ) {
    return RelativeScopeFactory.inputReadMethodsOf(factoryClass)
      .stream()
      .map(Method::getName)
      .toList();
  }

  @SuppressWarnings("unused")
  static final class CollectionInputFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    private final List<Factory<? super Scope, ?>> inputs = List.of();

    public List<Factory<? super Scope, ?>> getInputs() {
      return inputs;
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class MapInputFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    private final Map<String, Factory<? super Scope, ?>> inputs = Map.of();

    public Map<String, Factory<? super Scope, ?>> getInputs() {
      return inputs;
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class CollectionHeldFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    private final List<Factory<? super SimulationScope, ?>> childFactories =
      List.of();

    public List<Factory<? super SimulationScope, ?>> getChildFactories() {
      return childFactories;
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class PlainPropertyFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    public String getName() {
      return "plain";
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class DirectInputFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    private final Factory<? super Scope, ?> input = scope -> new Object();

    public Factory<? super Scope, ?> getInput() {
      return input;
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class DirectHeldFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    private final Factory<? super SimulationScope, ?> childFactory = scope ->
      new Object();

    public Factory<? super SimulationScope, ?> getChildFactory() {
      return childFactory;
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class CollectionUntypedFactory
    extends RelativeScopeFactory<Scope, Object>
  {

    private final List<Factory<?, ?>> inputs = List.of();

    public List<Factory<?, ?>> getInputs() {
      return inputs;
    }

    @Override
    protected Object newInstance(final Scope scope) {
      return new Object();
    }
  }

  private abstract static class LeftScope extends Scope {}

  private abstract static class RightScope extends Scope {}

  private abstract static class LeftLevelFactory
    extends AbstractFactory<LeftScope, Object>
  {

    @Override
    protected Class<LeftScope> scopeClass() {
      return LeftScope.class;
    }

    @Override
    protected Object newInstance(final LeftScope scope) {
      return new Object();
    }
  }

  private abstract static class RightLevelFactory
    extends AbstractFactory<RightScope, Object>
  {

    @Override
    protected Class<RightScope> scopeClass() {
      return RightScope.class;
    }

    @Override
    protected Object newInstance(final RightScope scope) {
      return new Object();
    }
  }

  @SuppressWarnings("unused")
  static final class TwoScopesFactory<S extends Scope> extends RelativeScopeFactory<
    S,
    Object
  > {

    private final Factory<? super S, ?> left;
    private final Factory<? super S, ?> right;

    TwoScopesFactory(
      final Factory<? super S, ?> left,
      final Factory<? super S, ?> right
    ) {
      this.left = left;
      this.right = right;
    }

    public Factory<? super S, ?> getLeft() {
      return left;
    }

    public Factory<? super S, ?> getRight() {
      return right;
    }

    @Override
    protected Object newInstance(final S scope) {
      return new Object();
    }
  }
}
