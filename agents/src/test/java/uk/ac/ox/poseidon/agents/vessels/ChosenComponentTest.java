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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.agents.vessels.Factories.by;
import static uk.ac.ox.poseidon.agents.vessels.Factories.choice;
import static uk.ac.ox.poseidon.agents.vessels.Factories.none;
import static uk.ac.ox.poseidon.agents.vessels.Factories.otherwise;
import static uk.ac.ox.poseidon.agents.vessels.Factories.when;
import static uk.ac.ox.poseidon.agents.vessels.values.Factories.componentProperty;
import static uk.ac.ox.poseidon.core.utils.Factories.object;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.values.ComponentPropertyFactory;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

/**
 * A choice picks the row of a vessel's values, or its default, and fails on
 * a table that cannot be read one way only, and on a vessel it cannot choose
 * for.
 */
class ChosenComponentTest {

  private final Factory<VesselComponentScope, Object> ps = _ -> "PS";
  private final Factory<VesselComponentScope, Object> large = _ -> "large";
  private final Factory<VesselComponentScope, Object> other = _ -> "other";

  private static Vessel vesselWith(final Map<String, Object> attributes) {
    final Vessel vessel = mock(Vessel.class);
    Mockito.when(vessel.getId()).thenReturn("V1");
    Mockito.when(vessel.getAttributes()).thenReturn(attributes);
    return vessel;
  }

  private static Object factoryOf(final ChoiceOutcome<?> outcome) {
    return ((BuildOutcome<?>) outcome).getFactory();
  }

  @Test
  void theRowOfTheVesselsValuesIsPicked() {
    final ChosenComponent<Object> chosen = choice(
      by("gear", "size"),
      when("PS", "small").then(ps),
      when("PS", "large").then(large)
    );

    assertThat(
      factoryOf(
        chosen.outcomeFor(vesselWith(Map.of("gear", "PS", "size", "large")))
      )
    ).isSameAs(large);
  }

  @Test
  void valuesNoRowListsTakeTheDefault() {
    final ChosenComponent<Object> chosen = choice(
      by("gear"),
      when("PS").then(ps),
      otherwise(other)
    );

    assertThat(
      factoryOf(chosen.outcomeFor(vesselWith(Map.of("gear", "OTB"))))
    ).isSameAs(other);
  }

  @Test
  void aRowOrADefaultMayGiveNoComponent() {
    final ChosenComponent<Object> chosen = choice(
      by("gear"),
      when("GNS").then(none()),
      otherwise(none())
    );

    assertThat(
      chosen.outcomeFor(vesselWith(Map.of("gear", "GNS")))
    ).isInstanceOf(NoComponent.class);
    assertThat(
      chosen.outcomeFor(vesselWith(Map.of("gear", "OTB")))
    ).isInstanceOf(NoComponent.class);
  }

  @Test
  void valuesNoRowListsWithNoDefaultAreAnErrorNamingTheVesselAndTheValues() {
    final ChosenComponent<Object> chosen = choice(
      by("gear"),
      when("PS").then(ps)
    );

    assertThatThrownBy(() ->
      chosen.outcomeFor(vesselWith(Map.of("gear", "OTB")))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("gear")
      .hasMessageContaining("OTB");
  }

  @Test
  void anAbsentAttributeIsAnErrorEvenWithADefault() {
    final ChosenComponent<Object> chosen = choice(
      by("gear"),
      when("PS").then(ps),
      otherwise(other)
    );

    assertThatThrownBy(() -> chosen.outcomeFor(vesselWith(Map.of())))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Vessel V1 has no attribute gear");
  }

  @Test
  void twoRowsWithTheSameValuesAreAnError() {
    assertThatThrownBy(() ->
      choice(by("gear"), when("PS").then(ps), when("PS").then(other))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("two rows")
      .hasMessageContaining("PS");
  }

  @Test
  void aRowWithTheWrongNumberOfValuesIsAnError() {
    assertThatThrownBy(() ->
      choice(by("gear", "size"), when("PS").then(ps))
    ).isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void twoDefaultsAreAnError() {
    assertThatThrownBy(() ->
      choice(by("gear"), otherwise(ps), otherwise(none()))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("two defaults");
  }

  @Test
  void survivesAYamlRoundTrip() {
    final ChosenComponent<Object> chosen = choice(
      by("gear"),
      when("PS").then(object("ps")),
      when("GNS").then(none()),
      otherwise(object("other"))
    );
    final String written = new ScenarioWriter().write(
      Scenario.builder()
        .component("size", componentProperty(chosen, "size"))
        .build()
    );

    final ComponentPropertyFactory<?> loaded =
      (ComponentPropertyFactory<?>) new ScenarioLoader()
        .load(written)
        .getComponents()
        .get("size");
    final ChosenComponent<?> loadedChoice =
      (ChosenComponent<?>) loaded.getComponent();
    assertThat(loadedChoice.getAttributes()).containsExactly("gear");
    assertThat(
      factoryOf(loadedChoice.outcomeFor(vesselWith(Map.of("gear", "PS"))))
    ).isEqualTo(object("ps"));
    assertThat(
      loadedChoice.outcomeFor(vesselWith(Map.of("gear", "GNS")))
    ).isInstanceOf(NoComponent.class);
    assertThat(
      factoryOf(loadedChoice.outcomeFor(vesselWith(Map.of("gear", "OTB"))))
    ).isEqualTo(object("other"));
  }

  @Test
  void rowsOfTwoGearClassesGiveAComponentOfAGear() {
    final Factory<VesselComponentScope, SeineGear> seine = _ -> new SeineGear();
    final Factory<VesselComponentScope, TrawlGear> trawl = _ -> new TrawlGear();

    final var gear = choice(
      by("gear"),
      when("PS").then(seine),
      when("OTB").then(trawl),
      otherwise(none())
    );

    assertThat(takesAGear(gear).getRows()).hasSize(2);
  }

  private static ChosenComponent<? extends Gear> takesAGear(
    final ChosenComponent<? extends Gear> gear
  ) {
    return gear;
  }

  private abstract static class TestGear implements Gear {

    @Override
    public Supplier<Duration> getDurationSupplier() {
      return () -> Duration.ZERO;
    }

    @Override
    public Bucket fish(final Fisheable fisheable) {
      return Bucket.empty();
    }
  }

  private static final class SeineGear extends TestGear {}

  private static final class TrawlGear extends TestGear {}
}
