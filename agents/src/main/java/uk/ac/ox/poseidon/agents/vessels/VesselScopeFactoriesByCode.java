/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Map;
import java.util.Optional;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;

/**
 * A {@link Factory} that picks which of several factories to resolve based on
 * {@link #code} (e.g. a gear type or fleet segment code), falling back to
 * {@link #defaultFactory} if {@link #code} has no entry in {@link #factories}.
 * Built directly via its {@code @SuperBuilder}, not from {@code Factories}.
 * <p>
 * The vessel register sets {@link #code} from each row before asking for a
 * vessel's component, so this factory keeps no product of its own: each call
 * resolves the factory picked for the current code, whose product is kept in
 * the vessel's store.
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VesselScopeFactoriesByCode<P> implements Factory<VesselScope, P> {

  @Singular
  private Map<
    String,
    ? extends Factory<? super VesselScope, ? extends P>
  > factories;

  private Factory<? super VesselScope, ? extends P> defaultFactory;
  private String code;

  /**
   * @return the resolved value from {@link #factories}' entry for
   *         {@link #code}, or {@link #defaultFactory}'s if there's no entry for
   *         {@link #code}
   * @throws NullPointerException     if {@link #code} isn't set
   * @throws IllegalArgumentException if neither {@link #factories} nor
   *                                  {@link #defaultFactory} has an answer for
   *                                  {@link #code}
   */
  @Override
  public P get(final VesselScope scope) {
    checkNotNull(code, "Cannot create new instance unless code is set.");
    return Optional.ofNullable(factories.get(code))
      .map(factory -> (P) factory.get(scope))
      .or(() ->
        Optional.ofNullable(defaultFactory).map(factory -> factory.get(scope))
      )
      .orElseThrow(() ->
        new IllegalArgumentException(
          "No factory found for code %s and no default factory provided.".formatted(
            code
          )
        )
      );
  }
}
