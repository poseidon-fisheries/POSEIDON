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

import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import java.util.Optional;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A component that varies by one vessel attribute. Each listed value maps to
 * the factory that builds the component for it; {@link #unlistedValueHandling}
 * says what a value that is not listed gives. The listed values are strings,
 * since the component selects by category. A missing attribute or a value that
 * is not a string is an error.
 * <p>
 * The vessel builds the selected factory's component on creation and again
 * whenever the attribute changes, even if the same factory is selected.
 * Building this factory only builds a reader of the vessel's stored component.
 * Components are compared by identity, as the keys under which vessels store
 * them. Built via {@link Factories Factories.varyingBy(...)}.
 *
 * @param <T> the type of the component
 */
@Getter
public final class VaryingComponent<T>
  extends GlobalScopeFactory<Function<Vessel, T>>
  implements VesselComponent<T>
{

  /** The vessel attribute that determines the factory. */
  @NonNull
  private final String attribute;

  /** The factories for the explicitly listed attribute values. */
  @NonNull
  private final ImmutableMap<
    String,
    Factory<? super VesselComponentScope, ? extends T>
  > factories;

  /** What a vessel with an unlisted value gets. */
  @NonNull
  private final UnlistedValueHandling unlistedValueHandling;

  @Builder(toBuilder = true)
  private VaryingComponent(
    @NonNull final String attribute,
    @NonNull @Singular final ImmutableMap<
      String,
      Factory<? super VesselComponentScope, ? extends T>
    > factories,
    @NonNull final UnlistedValueHandling unlistedValueHandling
  ) {
    this.attribute = attribute;
    this.factories = factories;
    this.unlistedValueHandling = unlistedValueHandling;
  }

  @Override
  protected Function<Vessel, T> newInstance(final Scope scope) {
    return this::of;
  }

  /** @return whether {@code other} is this very component */
  @Override
  public boolean equals(final Object other) {
    return this == other;
  }

  @Override
  public int hashCode() {
    return System.identityHashCode(this);
  }

  /**
   * @param vessel the vessel whose attribute determines the factory
   * @return the listed value's factory, or nothing if the value is unlisted
   *         and unlisted values give no component
   * @throws IllegalStateException if the attribute is missing, is not a
   *                               string, or is unlisted and unlisted values
   *                               are an error
   */
  Optional<Factory<? super VesselComponentScope, ? extends T>> factoryFor(
    final Vessel vessel
  ) {
    final Object value = vessel.getAttributes().get(attribute);
    checkState(
      value != null,
      "Vessel %s has no attribute %s",
      vessel.getId(),
      attribute
    );
    checkState(
      value instanceof String,
      "Vessel %s has value %s of type %s for %s, but the varying component requires a String",
      vessel.getId(),
      value,
      value.getClass().getName(),
      attribute
    );
    final var factory = factories.get(value);
    checkState(
      factory != null ||
        unlistedValueHandling == UnlistedValueHandling.NO_COMPONENT,
      "Vessel %s has the value %s for %s, which the varying component does not list",
      vessel.getId(),
      value,
      attribute
    );
    return Optional.ofNullable(factory);
  }
}
