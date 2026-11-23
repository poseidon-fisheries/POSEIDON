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

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;

class ScenarioLoaderTest {

  @Test
  void rejectsDuplicateScenarioProperties() {
    final String yaml = """
    components: {}
    components: {}
    """;

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(DuplicateKeyException.class)
      .hasMessageContaining("components");
  }

  @Test
  void rejectsDuplicateComponentNames() {
    final String yaml = """
    components:
      gear: !!uk.ac.ox.poseidon.core.utils.ObjectFactory {value: first}
      gear: !!uk.ac.ox.poseidon.core.utils.ObjectFactory {value: second}
    """;

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(DuplicateKeyException.class)
      .hasMessageContaining("duplicate key gear");
  }

  @Test
  void rejectsDuplicateFactoryProperties() {
    final String yaml = """
    components:
      gear: !!uk.ac.ox.poseidon.core.utils.ObjectFactory
        value: first
        value: second
    """;

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(DuplicateKeyException.class)
      .hasMessageContaining("duplicate key value");
  }

  @Test
  void rejectsDuplicateKeysInNestedMaps() {
    final String yaml = """
    components:
      gear: !!uk.ac.ox.poseidon.core.utils.ObjectFactory
        value:
          PS: first
          PS: second
    """;

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(DuplicateKeyException.class)
      .hasMessageContaining("duplicate key PS");
  }

  @Test
  void loadsDistinctKeysAndAllowsTheSameKeyInSeparateMaps() {
    final String yaml = """
    components:
      gear: !!uk.ac.ox.poseidon.core.utils.ObjectFactory
        value: {PS: seine, OTB: trawl}
      engine: !!uk.ac.ox.poseidon.core.utils.ObjectFactory
        value: {PS: first, OTB: second}
    """;

    final var scenario = new ScenarioLoader().load(yaml);

    assertThat(scenario.getComponents()).containsOnlyKeys("gear", "engine");
    assertThat(scenario.getComponents().get("gear")).isEqualTo(
      ObjectFactory.builder()
        .value(Map.of("PS", "seine", "OTB", "trawl"))
        .build()
    );
    assertThat(scenario.getComponents().get("engine")).isEqualTo(
      ObjectFactory.builder()
        .value(Map.of("PS", "first", "OTB", "second"))
        .build()
    );
  }

  @Test
  void rejectsASetWhereAnObjectFactoryHoldsAValue() {
    final String yaml = """
    components:
      members: !!uk.ac.ox.poseidon.core.utils.ObjectFactory
        value: !!set {a, b}
    """;

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .hasRootCauseInstanceOf(IllegalArgumentException.class)
      .hasRootCauseMessage("value must be a literal, not [a, b].");
  }
}
