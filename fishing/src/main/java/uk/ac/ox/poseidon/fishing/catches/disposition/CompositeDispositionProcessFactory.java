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

package uk.ac.ox.poseidon.fishing.catches.disposition;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;

/**
 * A {@link RelativeScopeFactory} counterpart of
 * {@link CompositeDispositionProcess}, built via
 * {@link Factories#compositeDispositionProcess}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CompositeDispositionProcessFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  CompositeDispositionProcess
> {

  @Singular
  private final ImmutableList<
    Factory<? super S, ? extends DispositionProcess>
  > dispositionStrategies;

  /** @param dispositionStrategies the steps to chain, in order */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public CompositeDispositionProcessFactory(
    final Factory<
      ? super S,
      ? extends DispositionProcess
    >... dispositionStrategies
  ) {
    this(ImmutableList.copyOf(dispositionStrategies));
  }

  /**
   * @return a {@link CompositeDispositionProcess} chaining every resolved step,
   *         in order
   */
  @Override
  protected CompositeDispositionProcess newInstance(final S scope) {
    return new CompositeDispositionProcess(
      dispositionStrategies
        .stream()
        .map(f -> f.get(scope))
        .collect(toImmutableList())
    );
  }
}
