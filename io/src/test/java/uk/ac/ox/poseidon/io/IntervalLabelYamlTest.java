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

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.functions.Factories.intervalLabel;
import static uk.ac.ox.poseidon.core.providers.constant.Factories.always;

import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.constructor.DuplicateKeyException;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.functions.IntervalLabelFactory;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/** Interval definitions are written and loaded as label-to-bound maps. */
class IntervalLabelYamlTest {

  /** Always places the number 7. */
  private static final MappingFactory<Scope, Object, Object, Double> NUMBER =
    always(7.0);

  private static final Scenario SCENARIO = Scenario.builder()
    .startingDateTime(new DateTimeFactory(2000, 1, 1, 0, 0, 0))
    .component(
      "label",
      intervalLabel(
        NUMBER,
        entry("small", 0.0),
        entry("medium", 6.0),
        entry("large", 12.0)
      )
    )
    .build();

  @SuppressWarnings("unchecked")
  private static BiFunction<Object, Object, String> labelOf(
    final Scenario scenario
  ) {
    return (
      (IntervalLabelFactory<Scope, Object, Object>) scenario.component(
        "label",
        IntervalLabelFactory.class
      )
    ).get(GLOBAL_SCOPE);
  }

  @Test
  void isWrittenAsLabelsMappedToNumericBoundsAndSurvivesRoundTrips() {
    final var derived = SCENARIO.with("label.lowerBoundsByLabel(medium)", 8.0);
    final var writer = new ScenarioWriter();
    final var loader = new ScenarioLoader();

    final var yaml = writer.write(derived);
    final var loaded = loader.load(yaml);

    assertThat(yaml).containsPattern(
      "lowerBoundsByLabel: \\{\\s*small: 0.0,\\s*medium: 8.0,\\s*large: 12.0\\s*}"
    );
    assertThat(writer.write(loaded)).isEqualTo(yaml);
    assertThat(labelOf(loaded).apply(null, null)).isEqualTo("small");
    assertThat(
      labelOf(loader.load(writer.write(SCENARIO))).apply(null, null)
    ).isEqualTo("medium");
  }

  @Test
  void aDuplicateLabelIsRejected() {
    final var yaml = new ScenarioWriter()
      .write(SCENARIO)
      .replace("large: 12.0", "small: 12.0");

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(DuplicateKeyException.class)
      .hasMessageContaining("duplicate key small");
  }

  @Test
  void aDerivedDefinitionThatIsInvalidFailsOnceTheLoadedScenarioResolves() {
    final var loaded = new ScenarioLoader().load(
      new ScenarioWriter().write(
        SCENARIO.with("label.lowerBoundsByLabel(large)", 6.0)
      )
    );

    assertThatThrownBy(() -> labelOf(loaded)).isInstanceOf(
      IllegalArgumentException.class
    );
  }
}
