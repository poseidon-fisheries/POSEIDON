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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static uk.ac.ox.poseidon.core.utils.Factories.object;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.none;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.varyingBy;
import static uk.ac.ox.poseidon.fishing.vessels.Factories.when;
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

  private static Object factoryOf(final Variant<?> outcome) {
    return ((FactoryVariant<?>) outcome).getFactory();
  }

  @Test
  void selectsTheVariantForTheAttributeValue() {
    final var component = varyingBy("gear", when("PS", ps), when("OTB", other));

    assertThat(
      factoryOf(component.variantFor(vesselWith(Map.of("gear", "PS"))))
    ).isSameAs(ps);
    assertThat(
      factoryOf(component.variantFor(vesselWith(Map.of("gear", "OTB"))))
    ).isSameAs(other);
  }

  @Test
  void unlistedValuesTakeTheDefault() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      other,
      when("PS", ps)
    );

    assertThat(
      factoryOf(chosen.variantFor(vesselWith(Map.of("gear", "OTB"))))
    ).isSameAs(other);
  }

  @Test
  void aListedVariantOrADefaultMayGiveNoComponent() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      none(),
      when("GNS", none())
    );

    assertThat(
      chosen.variantFor(vesselWith(Map.of("gear", "GNS")))
    ).isInstanceOf(NoComponent.class);
    assertThat(
      chosen.variantFor(vesselWith(Map.of("gear", "OTB")))
    ).isInstanceOf(NoComponent.class);
  }

  @Test
  void unlistedValuesWithoutADefaultFailNamingTheVesselAndValue() {
    final VaryingComponent<Object> chosen = varyingBy("gear", when("PS", ps));

    assertThatThrownBy(() ->
      chosen.variantFor(vesselWith(Map.of("gear", "OTB")))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("gear")
      .hasMessageContaining("OTB");
  }

  @Test
  void anAbsentAttributeIsAnErrorEvenWithADefault() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      other,
      when("PS", ps)
    );

    assertThatThrownBy(() -> chosen.variantFor(vesselWith(Map.of())))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("Vessel V1 has no attribute gear");
  }

  @Test
  void duplicateKeysAreAnError() {
    assertThatThrownBy(() ->
      varyingBy("gear", when("PS", ps), when("PS", other))
    )
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("PS");
  }

  @Test
  void rejectsMixedKeyTypes() {
    assertThatThrownBy(() -> varyingBy("gear", when("PS", ps), when(1, other)))
      .isInstanceOf(IllegalArgumentException.class)
      .hasMessageContaining("mixed types");
  }

  @Test
  void rejectsTheWrongAttributeTypeOnAMissEvenWithADefault() {
    final var component = varyingBy("gear", other, when(1, ps));

    assertThatThrownBy(() ->
      component.variantFor(vesselWith(Map.of("gear", 1L)))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("V1")
      .hasMessageContaining("gear")
      .hasMessageContaining("java.lang.Long")
      .hasMessageContaining("java.lang.Integer");
  }

  @Test
  void aDefaultWithoutListedValuesStillRequiresTheAttribute() {
    final var component = varyingBy("gear", ps);

    assertThat(
      factoryOf(component.variantFor(vesselWith(Map.of("gear", 1L))))
    ).isSameAs(ps);
    assertThatThrownBy(() -> component.variantFor(vesselWith(Map.of())))
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("has no attribute gear");
  }

  @Test
  void anAbsentDefaultWithoutListedValuesStillRequiresTheAttribute() {
    final var component = varyingBy("gear", none());

    assertThat(
      component.variantFor(vesselWith(Map.of("gear", "PS")))
    ).isInstanceOf(NoComponent.class);
    assertThatThrownBy(() ->
      component.variantFor(vesselWith(Map.of()))
    ).isInstanceOf(IllegalStateException.class);
  }

  @Test
  void noVariantsAndNoDefaultCannotSelectAComponent() {
    final var component = varyingBy("gear");

    assertThatThrownBy(() ->
      component.variantFor(vesselWith(Map.of("gear", "PS")))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("no default");
  }

  @Test
  void survivesAYamlRoundTrip() {
    final VaryingComponent<Object> chosen = varyingBy(
      "gear",
      object("other"),
      when("PS", object("ps")),
      when("GNS", none())
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
    final VaryingComponent<?> loadedChoice =
      (VaryingComponent<?>) loaded.getComponent();
    assertThat(loadedChoice.getAttribute()).isEqualTo("gear");
    assertThat(
      factoryOf(loadedChoice.variantFor(vesselWith(Map.of("gear", "PS"))))
    ).isEqualTo(object("ps"));
    assertThat(
      loadedChoice.variantFor(vesselWith(Map.of("gear", "GNS")))
    ).isInstanceOf(NoComponent.class);
    assertThat(
      factoryOf(loadedChoice.variantFor(vesselWith(Map.of("gear", "OTB"))))
    ).isEqualTo(object("other"));
  }

  @Test
  void variantsOfTwoGearClassesGiveAComponentOfAGear() {
    final Factory<VesselComponentScope, SeineGear> seine = _ -> new SeineGear();
    final Factory<VesselComponentScope, TrawlGear> trawl = _ -> new TrawlGear();

    final var gear = varyingBy(
      "gear",
      none(),
      when("PS", seine),
      when("OTB", trawl)
    );

    assertThat(takesAGear(gear).getVariants()).hasSize(2);
    assertThat(
      takesAGear(varyingBy("gear", seine, when("OTB", trawl))).getVariants()
    ).hasSize(1);
    assertThat(
      takesAGear(
        varyingBy("gear", when("PS", seine), when("OTB", trawl))
      ).getVariants()
    ).hasSize(2);
  }

  private static VaryingComponent<? extends Gear> takesAGear(
    final VaryingComponent<? extends Gear> gear
  ) {
    return gear;
  }

  @Test
  void absentAndMissingDefaultsSurviveYamlRoundTrips() {
    final var absentDefault = roundTrip(
      varyingBy("gear", none(), when("PS", object("ps")))
    );
    final var noDefault = roundTrip(
      varyingBy("gear", when("PS", object("ps")))
    );

    assertThat(
      absentDefault.variantFor(vesselWith(Map.of("gear", "OTB")))
    ).isInstanceOf(NoComponent.class);
    assertThatThrownBy(() ->
      noDefault.variantFor(vesselWith(Map.of("gear", "OTB")))
    )
      .isInstanceOf(IllegalStateException.class)
      .hasMessageContaining("no default");
    assertThat(
      factoryOf(noDefault.variantFor(vesselWith(Map.of("gear", "PS"))))
    ).isEqualTo(object("ps"));
  }

  @Test
  void duplicateVariantKeysInYamlAreAnError() {
    final String yaml = """
    components:
      gearReader: !!uk.ac.ox.poseidon.fishing.vessels.values.ComponentPropertyFactory
        component: !!uk.ac.ox.poseidon.fishing.vessels.VaryingComponent
          attribute: gear
          variants:
            PS: !!uk.ac.ox.poseidon.fishing.vessels.NoComponent {}
            PS: !!uk.ac.ox.poseidon.fishing.vessels.NoComponent {}
        propertyPath: size
    """;

    assertThatThrownBy(() -> new ScenarioLoader().load(yaml))
      .isInstanceOf(org.yaml.snakeyaml.constructor.DuplicateKeyException.class)
      .hasMessageContaining("duplicate key PS");
  }

  @Test
  void mixedVariantKeyTypesInYamlAreAnError() {
    final String yaml = """
    components:
      gearReader: !!uk.ac.ox.poseidon.fishing.vessels.values.ComponentPropertyFactory
        component: !!uk.ac.ox.poseidon.fishing.vessels.VaryingComponent
          attribute: gear
          variants:
            PS: !!uk.ac.ox.poseidon.fishing.vessels.NoComponent {}
            1: !!uk.ac.ox.poseidon.fishing.vessels.NoComponent {}
        propertyPath: size
    """;

    assertThatThrownBy(() ->
      new ScenarioLoader().load(yaml)
    ).hasStackTraceContaining("mixed types");
  }

  private static VaryingComponent<?> roundTrip(
    final VaryingComponent<?> component
  ) {
    final String yaml = new ScenarioWriter().write(
      Scenario.builder()
        .component("reader", componentProperty(component, "size"))
        .build()
    );
    final var reader = (ComponentPropertyFactory<?>) new ScenarioLoader()
      .load(yaml)
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
    public Bucket fish(final Fisheable fisheable) {
      return Bucket.empty();
    }
  }

  private static final class SeineGear extends TestGear {}

  private static final class TrawlGear extends TestGear {}
}
