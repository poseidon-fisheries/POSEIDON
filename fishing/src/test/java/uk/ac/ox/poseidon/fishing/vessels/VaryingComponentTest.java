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

package uk.ac.ox.poseidon.fishing.vessels;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.varyingBy;
import static uk.ac.ox.poseidon.fishing.vessels.UnlistedValueHandling.ERROR;
import static uk.ac.ox.poseidon.fishing.vessels.UnlistedValueHandling.NO_COMPONENT;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.componentProperty;

import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.fishing.vessels.values.ComponentPropertyFactory;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

/**
 * A varying component selects by one required attribute, rejecting ambiguous
 * keys and wrong attribute types.
 */
class VaryingComponentTest {

  private final Factory<VesselComponentScope, Object> ps = _ -> "PS";
  private final Factory<VesselComponentScope, Object> other = _ -> "other";

  private static Vessel vesselWith(final Map<String, Object> attributes) {
    final Vessel vessel = mock(Vessel.class);
    Mockito.when(vessel.getId()).thenReturn("V1");
    Mockito.when(vessel.getAttributes()).thenReturn(attributes);
    return vessel;
  }

  /** The factory selected for the vessel; empty if it has no component. */
  private static Object factoryOf(
    final VaryingComponent<?> component,
    final Map<String, Object> attributes
  ) {
    return component.factoryFor(vesselWith(attributes)).orElse(null);
  }

  @Test
  void selectsTheFactoryForTheAttributeValue() {
    final var component = varyingBy(
      "gear",
      ERROR,
      entry("PS", ps),
      entry("OTB", other)
    );

    assertThat(factoryOf(component, Map.of("gear", "PS"))).isSameAs(ps);
    assertThat(factoryOf(component, Map.of("gear", "OTB"))).isSameAs(other);
  }

  @Test
  void aListedValueGivesItsFactoryUnderBothHandlings() {
    for (final var handling : UnlistedValueHandling.values()) {
      assertThat(
        factoryOf(
          varyingBy("gear", handling, entry("PS", ps)),
          Map.of("gear", "PS")
        )
      ).isSameAs(ps);
    }
  }

  @Test
  void unlistedValuesGiveNoComponentUnderNoComponent() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      NO_COMPONENT,
      entry("PS", ps)
    );

    assertThat(chosen.factoryFor(vesselWith(Map.of("gear", "OTB")))).isEmpty();
  }

  @Test
  void unlistedValuesFailNamingTheVesselAttributeAndValueUnderError() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      ERROR,
      entry("PS", ps)
    );

    assertThatThrownBy(() ->
      chosen.factoryFor(vesselWith(Map.of("gear", "OTB")))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("gear")
      .hasMessageContaining("OTB");
  }

  @Test
  void aMissingAttributeIsAnErrorUnderBothHandlings() {
    for (final var handling : UnlistedValueHandling.values()) {
      final VaryingComponent<Object> chosen = varyingBy(
        "gear",
        handling,
        entry("PS", ps)
      );

      assertThatThrownBy(() -> chosen.factoryFor(vesselWith(Map.of())))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Vessel V1 has no attribute gear");
    }
  }

  @Test
  void aWrongTypeValueIsAnErrorUnderBothHandlings() {
    for (final var handling : UnlistedValueHandling.values()) {
      final var component = varyingBy("gear", handling, entry(1, ps));

      assertThatThrownBy(() ->
        component.factoryFor(vesselWith(Map.of("gear", 1L)))
      )
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("V1")
        .hasMessageContaining("gear")
        .hasMessageContaining("java.lang.Long")
        .hasMessageContaining("java.lang.Integer");
    }
  }

  @Test
  void withNoListedValuesThereIsNoTypeToCheck() {
    final var none = varyingBy("gear", NO_COMPONENT);
    final var error = varyingBy("gear", ERROR);

    assertThat(none.factoryFor(vesselWith(Map.of("gear", "PS")))).isEmpty();
    assertThat(none.factoryFor(vesselWith(Map.of("gear", 1L)))).isEmpty();
    assertThatThrownBy(() ->
      error.factoryFor(vesselWith(Map.of("gear", "PS")))
    ).isInstanceOf(IllegalStateException.class);
    assertThatThrownBy(() ->
      none.factoryFor(vesselWith(Map.of()))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void duplicateKeysAreAnError() {
    assertThatThrownBy(() ->
      varyingBy("gear", ERROR, entry("PS", ps), entry("PS", other))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("PS");
  }

  @Test
  void rejectsMixedKeyTypes() {
    assertThatThrownBy(() ->
      varyingBy("gear", ERROR, entry("PS", ps), entry(1, other))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("mixed types");
  }

  @Test
  void survivesAYamlRoundTripKeepingValuesHandlingAndOrder() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      NO_COMPONENT,
      entry("PS", object("ps")),
      entry("GNS", object("gns")),
      entry("OTB", object("otb"))
    );

    final String written = write(chosen);
    final VaryingComponent<?> loaded = roundTrip(chosen);

    assertThat(written)
      .doesNotContain("Variant")
      .doesNotContain("NoComponent")
      .contains("unlistedValueHandling: NO_COMPONENT")
      .containsPattern("PS: !!uk\\.ac\\.ox\\.poseidon\\.core\\.utils\\.ObjectFactory");
    assertThat(loaded.getAttribute()).isEqualTo("gear");
    assertThat(loaded.getUnlistedValueHandling()).isEqualTo(NO_COMPONENT);
    assertThat(loaded.getFactories().keySet()).containsExactly(
      "PS",
      "GNS",
      "OTB"
    );
    assertThat(factoryOf(loaded, Map.of("gear", "PS"))).isEqualTo(object("ps"));
    assertThat(factoryOf(loaded, Map.of("gear", "OTB"))).isEqualTo(
      object("otb")
    );
    assertThat(factoryOf(loaded, Map.of("gear", "XYZ"))).isNull();
  }

  @Test
  void errorHandlingSurvivesAYamlRoundTrip() {
    final var loaded = roundTrip(
      varyingBy("gear", ERROR, entry("PS", object("ps")))
    );

    assertThat(loaded.getUnlistedValueHandling()).isEqualTo(ERROR);
    assertThatThrownBy(() ->
      loaded.factoryFor(vesselWith(Map.of("gear", "OTB")))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void factoriesOfTwoGearClassesGiveAComponentOfAGear() {
    final Factory<VesselComponentScope, SeineGear> seine = _ -> new SeineGear();
    final Factory<VesselComponentScope, TrawlGear> trawl = _ -> new TrawlGear();

    final var gear = takesAGear(
      varyingBy("gear", NO_COMPONENT, entry("PS", seine), entry("OTB", trawl))
    );

    assertThat(gear.getFactories()).hasSize(2);
  }

  private static VaryingComponent<? extends Gear> takesAGear(
    final VaryingComponent<? extends Gear> gear
  ) {
    return gear;
  }

  @Test
  void aCalibrationAddressIntoTheFactoryMapResolvesAndDerives() {
    final var gear = varyingBy(
      "gear",
      ERROR,
      entry("PS", object(1.0)),
      entry("OTB", object(2.0))
    );
    final Scenario scenario = Scenario.builder()
      .component("gear", gear)
      .build();

    final Scenario derived = scenario.with("gear.factories(OTB).value", 5.0);

    final var changed = derived.component("gear", VaryingComponent.class);
    assertThat(
      ((ObjectFactory<?>) changed.getFactories().get("OTB")).getValue()
    ).isEqualTo(5.0);
    assertThat(
      ((ObjectFactory<?>) changed.getFactories().get("PS")).getValue()
    ).isEqualTo(1.0);
    assertThat(changed.getUnlistedValueHandling()).isEqualTo(ERROR);
  }

  @Test
  void duplicateKeysInYamlAreAnError() {
    final String yaml = yamlWith(
      """
          PS: !!uk.ac.ox.poseidon.core.utils.ObjectFactory {value: 1}
          PS: !!uk.ac.ox.poseidon.core.utils.ObjectFactory {value: 2}
      """
    );

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(org.yaml.snakeyaml.constructor.DuplicateKeyException.class)
      .hasMessageContaining("duplicate key PS");
  }

  @Test
  void mixedKeyTypesInYamlAreAnError() {
    final String yaml = yamlWith(
      """
          PS: !!uk.ac.ox.poseidon.core.utils.ObjectFactory {value: 1}
          1: !!uk.ac.ox.poseidon.core.utils.ObjectFactory {value: 2}
      """
    );

    assertThatThrownBy(() ->
      new ScenarioLoader().load(yaml)
    ).hasStackTraceContaining("mixed types");
  }

  private static String yamlWith(final String factoryEntries) {
    return (
      """
      components:
        gearReader: !!uk.ac.ox.poseidon.fishing.vessels.values.ComponentPropertyFactory
          component: !!uk.ac.ox.poseidon.fishing.vessels.VaryingComponent
            attribute: gear
            unlistedValueHandling: ERROR
            factories:
      """ +
      factoryEntries.indent(4) +
      "    propertyPath: size\n"
    );
  }

  private static String write(final VaryingComponent<?> component) {
    return new ScenarioWriter().write(
      Scenario.builder()
        .component("reader", componentProperty(component, "size"))
        .build()
    );
  }

  private static VaryingComponent<?> roundTrip(
    final VaryingComponent<?> component
  ) {
    final var reader = (ComponentPropertyFactory<?>) new ScenarioLoader()
      .load(write(component))
      .getComponents()
      .get("reader");
    return (VaryingComponent<?>) reader.getComponent();
  }

  private abstract static class TestGear implements Gear {

    @Override
    public Supplier<Duration> getDurationSupplier() {
      return () -> Duration.ZERO;
    }

    @Override
    public Duration getExpectedDuration() {
      return Duration.ZERO;
    }

    @Override
    public Bucket fish(final Fisheable fisheable) {
      return Bucket.empty();
    }
  }

  private static final class SeineGear extends TestGear {}

  private static final class TrawlGear extends TestGear {}
}
