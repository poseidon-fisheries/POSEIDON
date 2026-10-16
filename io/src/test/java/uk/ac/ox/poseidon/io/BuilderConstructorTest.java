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

package uk.ac.ox.poseidon.io;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Singular;
import lombok.experimental.SuperBuilder;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import uk.ac.ox.poseidon.core.AbstractFactory;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

class BuilderConstructorTest {

  @Test
  void loadsObjectsThroughTheirBuilders() {
    final NumberFactory shared = NumberFactory.builder().value(1.5).build();
    final PairFactory<Double> pair = PairFactory.<Double>builder()
      .first(shared)
      .second(shared)
      .number(NumberFactory.builder().value(2.5).build())
      .item(NumberFactory.builder().value(3.5).build())
      .item(shared)
      .label("a pair")
      .build();

    final String yaml = writer().dump(pair);
    final PairFactory<?> loaded = loader(PairFactory.class).load(yaml);

    // Written as an anchor and an alias, read back as one object
    assertThat(yaml).contains("&id").contains("*id");
    assertThat(loaded.getFirst()).isSameAs(loaded.getSecond());
    assertThat(loaded.getItems().get(1)).isSameAs(loaded.getFirst());
    assertThat(((NumberFactory) loaded.getFirst()).getValue()).isEqualTo(1.5);
    // Written without a tag, since its class is the property's type
    assertThat(yaml).contains("number: {value: 2.5}");
    assertThat(loaded.getNumber().getValue()).isEqualTo(2.5);
    assertThat(loaded.getItems()).hasSize(2);
    assertThat(
      ((NumberFactory) loaded.getItems().getFirst()).getValue()
    ).isEqualTo(3.5);
    assertThat(loaded.getLabel()).isEqualTo("a pair");
    assertThat(writer().dump(loaded)).isEqualTo(yaml);
  }

  @Test
  void refusesAClassWithoutABuilder() {
    final String yaml =
      "!!" + NoBuilderFactory.class.getName() + " {value: 1.0}";
    assertThatThrownBy(() -> loader(Object.class).load(yaml))
      .rootCause()
      .hasMessageContaining(NoBuilderFactory.class.getName());
  }

  private static Yaml loader(final Class<?> rootType) {
    final LoaderOptions options = new LoaderOptions();
    options.setTagInspector(tag -> true);
    return new Yaml(
      new BuilderConstructor(rootType, List.of("uk.ac.ox.poseidon"), options)
    );
  }

  // Writes read-only properties, as the scenario writer does
  private static Yaml writer() {
    final DumperOptions options = new DumperOptions();
    options.setAllowReadOnlyProperties(true);
    return new Yaml(options);
  }

  @Getter
  @SuperBuilder
  public static class NumberFactory extends TestFactory<Double> {

    private final double value;

    @Override
    protected Double newInstance(final Scope scope) {
      return value;
    }
  }

  @Getter
  @SuperBuilder
  public static class PairFactory<T> extends TestFactory<Map<String, T>> {

    private final Factory<? super Scope, ? extends T> first;
    private final Factory<? super Scope, ? extends T> second;
    private final NumberFactory number;
    private final String label;

    @Singular
    private final List<Factory<? super Scope, ? extends T>> items;

    @Override
    protected Map<String, T> newInstance(final Scope scope) {
      return Map.of("first", first.get(scope), "second", second.get(scope));
    }
  }

  public static class NoBuilderFactory {

    public double value;
  }

  @SuperBuilder
  public abstract static class TestFactory<C> extends AbstractFactory<
    Scope,
    C
  > {

    @Override
    protected Object makeKey(final Scope scope) {
      return scope;
    }

    @Override
    protected Class<Scope> scopeClass() {
      return Scope.class;
    }
  }
}
