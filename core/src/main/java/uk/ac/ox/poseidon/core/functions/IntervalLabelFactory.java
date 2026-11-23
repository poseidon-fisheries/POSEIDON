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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import java.util.function.BiFunction;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link IntervalLabel}, with
 * labels that are strings, built via
 * {@link Factories Factories.intervalLabel(...)}.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class IntervalLabelFactory<S extends Scope, C, T>
  extends RelativeScopeFactory<S, BiFunction<C, T, String>>
  implements MappingFactory<S, C, T, String>
{

  private final MappingFactory<
    ? super S,
    ? super C,
    ? super T,
    ? extends Double
  > number;

  /**
   * Each interval's lower bound, by the interval's label. Labels are unique, and
   * so are lower bounds. An interval runs up to the next lower bound in
   * numerical order, whatever order the entries are written in, and the last
   * one has no upper bound.
   */
  @Singular("lowerBoundByLabel")
  private final ImmutableMap<String, Double> lowerBoundsByLabel;

  @Override
  protected BiFunction<C, T, String> newInstance(final S scope) {
    final var labelsByLowerBound = ImmutableSortedMap.<
      Double,
      String
    >naturalOrder();
    lowerBoundsByLabel.forEach((label, bound) ->
      labelsByLowerBound.put(bound, label)
    );
    return new IntervalLabel<>(number.get(scope), labelsByLowerBound.build());
  }
}
