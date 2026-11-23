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
import static uk.ac.ox.poseidon.core.time.Factories.dateTime;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.behaviour;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.fleet;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.varyingBy;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.vesselSpecification;
import static uk.ac.ox.poseidon.fishing.vessels.UnlistedValueHandling.ERROR;
import static uk.ac.ox.poseidon.fishing.vessels.UnlistedValueHandling.NO_COMPONENT;
import static uk.ac.ox.poseidon.fishing.vessels.values.Factories.componentProperty;

import com.badlogic.gdx.ai.btree.leaf.Success;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.biology.buckets.Bucket;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Scenario;
import uk.ac.ox.poseidon.core.utils.ObjectFactory;
import uk.ac.ox.poseidon.fishing.fields.VesselField;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.fishing.vessels.values.ComponentPropertyFactory;
import uk.ac.ox.poseidon.geography.ports.PortGrid;
import uk.ac.ox.poseidon.io.ScenarioLoader;
import uk.ac.ox.poseidon.io.ScenarioWriter;

/**
 * A varying component selects by one required attribute, rejecting ambiguous
 * keys and wrong attribute types.
 */
class VaryingComponentTest {

  private final Factory<VesselComponentScope, Object> ps = _ -> "PS";
  private final Factory<VesselComponentScope, Object> other = _ -> "other";

  private static <T> Optional<T> componentInSimulation(
    final VesselComponent<T> component,
    final Map<String, Object> attributes
  ) {
    final var fleet = fleet(
      object(mock(VesselField.class)),
      object(mock(PortGrid.class))
    );
    final var specification = vesselSpecification(
      fleet,
      behaviour(scope -> new Success<>()),
      entry("gear", component)
    );
    final var simulation = Scenario.builder()
      .startingDateTime(dateTime(LocalDateTime.of(2001, 1, 1, 0, 0)))
      .component("fleet", fleet)
      .component("specification", specification)
      .build()
      .startNewSimulation();
    final Optional<T> product;
    try {
      final Vessel vessel = simulation
        .getComponent(VesselSpecification.class)
        .create("V1", Optional.empty(), attributes);
      simulation.schedule.step(simulation);
      product = component.findIn(vessel);
      product.ifPresent(value ->
        assertThat(component.of(vessel)).isSameAs(value)
      );
    } finally {
      simulation.finish();
    }
    return product;
  }

  @Test
  void listedValuesGiveTheirFactoriesComponentsUnderBothHandlings() {
    for (final var handling : UnlistedValueHandling.values()) {
      final var component = varyingBy(
        "gear",
        handling,
        entry("PS", ps),
        entry("OTB", other)
      );

      assertThat(
        componentInSimulation(component, Map.of("gear", "PS"))
      ).contains("PS");
      assertThat(
        componentInSimulation(component, Map.of("gear", "OTB"))
      ).contains("other");
    }
  }

  @Test
  void unlistedValuesGiveNoComponentUnderNoComponent() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      NO_COMPONENT,
      entry("PS", ps)
    );

    assertThat(componentInSimulation(chosen, Map.of("gear", "OTB"))).isEmpty();
  }

  @Test
  void unlistedValuesFailNamingTheVesselAttributeAndValueUnderError() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      ERROR,
      entry("PS", ps)
    );

    assertThatThrownBy(() ->
      componentInSimulation(chosen, Map.of("gear", "OTB"))
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

      assertThatThrownBy(() -> componentInSimulation(chosen, Map.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Vessel V1 has no attribute gear");
    }
  }

  @Test
  void aWrongTypeValueIsAnErrorUnderBothHandlings() {
    for (final var handling : UnlistedValueHandling.values()) {
      final var component = varyingBy("gear", handling, entry(1, ps));

      assertThatThrownBy(() ->
        componentInSimulation(component, Map.of("gear", 1L))
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
    final var component = varyingBy("gear", NO_COMPONENT);

    assertThat(
      componentInSimulation(component, Map.of("gear", "PS"))
    ).isEmpty();
    assertThat(componentInSimulation(component, Map.of("gear", 1L))).isEmpty();
  }

  @Test
  void withNoListedValuesErrorHandlingRejectsEveryValue() {
    final var component = varyingBy("gear", ERROR);

    assertThatThrownBy(() ->
      componentInSimulation(component, Map.of("gear", "PS"))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("gear")
      .hasMessageContaining("PS");
    assertThatThrownBy(() ->
      componentInSimulation(component, Map.of("gear", 1L))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("gear")
      .hasMessageContaining("1");
  }

  @Test
  void withNoListedValuesTheAttributeIsStillRequiredUnderBothHandlings() {
    for (final var handling : UnlistedValueHandling.values()) {
      final var component = varyingBy("gear", handling);

      assertThatThrownBy(() -> componentInSimulation(component, Map.of()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("Vessel V1 has no attribute gear");
    }
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
      .containsPattern(
        "PS: !!uk\\.ac\\.ox\\.poseidon\\.core\\.utils\\.ObjectFactory"
      );
    assertThat(loaded.getAttribute()).isEqualTo("gear");
    assertThat(loaded.getUnlistedValueHandling()).isEqualTo(NO_COMPONENT);
    assertThat(loaded.getFactories().keySet()).containsExactly(
      "PS",
      "GNS",
      "OTB"
    );
    assertThat(
      componentInSimulation(loaded, Map.of("gear", "PS"))
    ).hasValueSatisfying(value -> assertThat(value).isEqualTo("ps"));
    assertThat(
      componentInSimulation(loaded, Map.of("gear", "OTB"))
    ).hasValueSatisfying(value -> assertThat(value).isEqualTo("otb"));
    assertThat(componentInSimulation(loaded, Map.of("gear", "XYZ"))).isEmpty();
  }

  @Test
  void errorHandlingSurvivesAYamlRoundTrip() {
    final var loaded = roundTrip(
      varyingBy("gear", ERROR, entry("PS", object("ps")))
    );

    assertThat(loaded.getUnlistedValueHandling()).isEqualTo(ERROR);
    assertThatThrownBy(() ->
      componentInSimulation(loaded, Map.of("gear", "OTB"))
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

    final var derivedComponent = derived.component(
      "gear",
      VaryingComponent.class
    );
    assertThat(
      ((ObjectFactory<?>) derivedComponent.getFactories().get("OTB")).getValue()
    ).isEqualTo(5.0);
    assertThat(
      ((ObjectFactory<?>) derivedComponent.getFactories().get("PS")).getValue()
    ).isEqualTo(1.0);
    assertThat(derivedComponent.getUnlistedValueHandling()).isEqualTo(ERROR);
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
