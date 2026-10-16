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

package uk.ac.ox.poseidon.core.providers;

import java.util.function.DoubleSupplier;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.providers.constant.ConstantDouble;
import uk.ac.ox.poseidon.core.scopes.Scope;

/**
 * A {@link RelativeScopeFactory} that resolves the given delegate
 * {@link DoubleSupplier} once, when built, and returns a {@link DoubleProvider}
 * that keeps handing out that same first-resolved value thereafter — a
 * snapshot, not a live pass-through to the delegate. No separate plain
 * component class here: the produced value is a {@link ConstantDouble} wrapping
 * the snapshot, built via {@link Factories#firstDoubleFrom(Factory)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FirstDoubleFromFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  DoubleProvider
> {

  private final Factory<? super S, ? extends DoubleSupplier> delegate;

  @Override
  protected DoubleProvider newInstance(final S scope) {
    return new ConstantDouble(delegate.get(scope).getAsDouble());
  }
}
