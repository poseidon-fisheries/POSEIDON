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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.collect.ImmutableMap;
import java.util.function.Function;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A component that varies by one vessel attribute. Each listed value gives
 * a factory or deliberately no component, with an optional default for values
 * not listed. All listed values have the same runtime type. A missing
 * attribute, a value of a different type, or an unlisted value without a
 * default is an error. With no listed values, there is no attribute type
 * to check.
 * <p>
 * The vessel builds the selected variant on creation and again whenever the
 * attribute changes, even if the same variant is selected. Building this
 * factory only builds a reader of the vessel's stored component. Components
 * are compared by identity, as the keys under which vessels store them.
 * Built via {@link Factories Factories.varyingBy(...)}.
 *
 * @param <T> the type of the component
 */
@Getter
public final class VaryingComponent<T>
  extends GlobalScopeFactory<Function<Vessel, T>>
  implements VesselComponent<T>
{

  /** The vessel attribute that determines the variant. */
  @NonNull
  private final String attribute;

  /** The variants for the explicitly listed attribute values. */
  @NonNull
  private final ImmutableMap<Object, Variant<? extends T>> variants;

  /** The default variant, or {@code null} when unlisted values are errors. */
  private final Variant<? extends T> defaultVariant;

  /**
   * @throws IllegalArgumentException if the listed values have mixed types
   */
  @Builder(toBuilder = true)
  private VaryingComponent(
    @NonNull final String attribute,
    @NonNull @Singular final ImmutableMap<
      Object,
      Variant<? extends T>
    > variants,
    final Variant<? extends T> defaultVariant
  ) {
    checkArgument(
      variants.keySet().stream().map(Object::getClass).distinct().count() <= 1,
      "The varying component by %s has keys of mixed types: %s",
      attribute,
      variants.keySet()
    );
    this.attribute = attribute;
    this.variants = variants;
    this.defaultVariant = defaultVariant;
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
   * @param vessel the vessel whose attribute determines the variant
   * @return the listed variant or the default
   * @throws IllegalStateException if the attribute is missing, has the wrong
   *                               type, or is unlisted without a default
   */
  Variant<? extends T> variantFor(final Vessel vessel) {
    final Object value = vessel.getAttributes().get(attribute);
    checkState(
      value != null,
      "Vessel %s has no attribute %s",
      vessel.getId(),
      attribute
    );
    final Variant<? extends T> variant = variants.get(value);
    if (variant == null && !variants.isEmpty()) {
      final Class<?> keyType = variants.keySet().iterator().next().getClass();
      checkState(
        value.getClass().equals(keyType),
        "Vessel %s has value %s of type %s for %s, but the varying component requires %s",
        vessel.getId(),
        value,
        value.getClass().getName(),
        attribute,
        keyType.getName()
      );
    }
    final Variant<? extends T> selected =
      variant == null ? defaultVariant : variant;
    checkState(
      selected != null,
      "Vessel %s has the value %s for %s, which the varying component does not list, and it has no default",
      vessel.getId(),
      value,
      attribute
    );
    return selected;
  }
}
