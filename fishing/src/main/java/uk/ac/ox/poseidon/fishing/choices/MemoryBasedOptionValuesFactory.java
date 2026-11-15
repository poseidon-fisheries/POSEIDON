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

package uk.ac.ox.poseidon.fishing.choices;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link MemoryBasedOptionValues},
 * built via {@link Factories#memoryBasedOptionValues(Factory, Factory)} or
 * {@link Factories#memoryBasedOptionValues(Factory)} for remembered returns.
 * @param <S> the scope type
 * @param <O> the option type
 * @param <M> the remembered outcome type
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class MemoryBasedOptionValuesFactory<
  S extends Scope,
  O,
  M
> extends RelativeScopeFactory<S, MemoryBasedOptionValues<O, M>> {

  private final Factory<
    ? super S,
    ? extends Function<? super Vessel, ? extends MemoryEntries<O, M>>
  > memorySelector;
  // Deliberate absence selects direct reading of remembered Double returns.
  private final Factory<? super S, ? extends Valuation<? super M>> valuation;

  @Override
  protected MemoryBasedOptionValues<O, M> newInstance(final S scope) {
    return new MemoryBasedOptionValues<>(
      memorySelector.get(scope),
      valuation == null
        ? vessel -> recollection ->
            Double.class.cast(recollection).doubleValue()
        : valuation.get(scope)
    );
  }
}
