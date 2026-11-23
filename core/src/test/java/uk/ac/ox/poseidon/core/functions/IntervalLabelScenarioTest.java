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

package uk.ac.ox.poseidon.core.functions;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static uk.ac.ox.poseidon.core.Scope.GLOBAL_SCOPE;
import static uk.ac.ox.poseidon.core.functions.Factories.intervalLabel;

import java.util.function.BiFunction;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.time.DateTimeFactory;

/** A lower bound is replaced through an ordinary mapped address. */
class IntervalLabelScenarioTest {

  private static final MappingFactory<Scope, Object, Double, Double> NUMBER =
    scope -> (context, n) -> n;

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
  private static BiFunction<Object, Double, String> labelOf(
    final Scenario scenario
  ) {
    return (
      (IntervalLabelFactory<Scope, Object, Double>) scenario.component(
        "label",
        IntervalLabelFactory.class
      )
    ).get(GLOBAL_SCOPE);
  }

  @Test
  void replacingABoundByItsLabelMovesTheBoundaryAndKeepsTheLabels() {
    final var derived = SCENARIO.with("label.lowerBoundsByLabel(medium)", 8.0);

    final var label = labelOf(derived);
    assertThat(label.apply(null, 7.0)).isEqualTo("small");
    assertThat(label.apply(null, 8.0)).isEqualTo("medium");
    assertThat(label.apply(null, 12.0)).isEqualTo("large");
    assertThat(labelOf(SCENARIO).apply(null, 7.0)).isEqualTo("medium");
  }

  @Test
  void aReplacedBoundIsOrderedNumericallyWhenTheLabellerResolves() {
    final var label = labelOf(
      SCENARIO.with("label.lowerBoundsByLabel(small)", 20.0)
    );

    assertThat(label.apply(null, 5.0)).isNull();
    assertThat(label.apply(null, 7.0)).isEqualTo("medium");
    assertThat(label.apply(null, 13.0)).isEqualTo("large");
    assertThat(label.apply(null, 20.0)).isEqualTo("small");
  }

  @Test
  void aDerivedBoundThatIsNaNIsRejectedWhenTheLabellerResolves() {
    final var derived = SCENARIO.with(
      "label.lowerBoundsByLabel(medium)",
      Double.NaN
    );

    assertThatThrownBy(() -> labelOf(derived)).isInstanceOf(
      IllegalArgumentException.class
    );
  }

  @Test
  void aDerivedBoundEqualToAnotherIsRejectedWhenTheLabellerResolves() {
    final var derived = SCENARIO.with("label.lowerBoundsByLabel(medium)", 0.0);

    assertThatThrownBy(() -> labelOf(derived)).isInstanceOf(
      IllegalArgumentException.class
    );
  }

  @Test
  void anUnknownLabelIsNotAnAddress() {
    assertThatThrownBy(() ->
      SCENARIO.with("label.lowerBoundsByLabel(huge)", 1.0)
    ).isInstanceOf(IllegalArgumentException.class);
  }
}
