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

package uk.ac.ox.poseidon.fishing.vessels.values;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.function.Function;
import uk.ac.ox.poseidon.core.functions.ObjectProperty;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;

/**
 * Reads a property of a vessel's component, following a dotted path of
 * getters from what the component built for the vessel, e.g.
 * {@code "availableCapacityInKg"} of its hold.
 *
 * @param <R> the type of the property; not checked
 */
public class ComponentProperty<R> implements Function<Vessel, R> {

  private final VesselComponent<?> component;
  private final String propertyPath;

  // The getters are found on the component's own class, which a choice may
  // vary from vessel to vessel
  private final ClassValue<ObjectProperty<Object, R>> properties =
    new ClassValue<>() {
      @SuppressWarnings("unchecked")
      @Override
      protected ObjectProperty<Object, R> computeValue(final Class<?> type) {
        return new ObjectProperty<>((Class<Object>) type, propertyPath);
      }
    };

  /**
   * @param component    the component to read
   * @param propertyPath the dotted path of properties to follow from it
   */
  public ComponentProperty(
    final VesselComponent<?> component,
    final String propertyPath
  ) {
    this.component = checkNotNull(component);
    this.propertyPath = checkNotNull(propertyPath);
  }

  @Override
  public R apply(final Vessel vessel) {
    final Object value = component.of(vessel);
    return properties.get(value.getClass()).apply(null, value);
  }
}
