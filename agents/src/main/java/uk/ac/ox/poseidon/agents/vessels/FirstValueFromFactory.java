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

package uk.ac.ox.poseidon.agents.vessels;

import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.core.providers.constant.Constant;

/**
 * A {@link VesselScopeFactory} that asks the given value once per vessel, when
 * built, and produces a {@link Constant} of that first answer — a snapshot, not
 * a live pass-through to the value, so a random draw, for instance, is drawn
 * once for each vessel and kept. The value is asked with no context. Its level
 * is declared, not derived: asking the value can change what gave it (a draw
 * moves the random number generator), so the level says how many times it is
 * asked. Built via {@link Factories#firstValueFrom(ValueFactory)}.
 *
 * @param <R> the type of the value
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FirstValueFromFactory<R>
  extends VesselScopeFactory<Function<Object, R>>
  implements ValueFactory<VesselScope, Object, R>
{

  private final ValueFactory<
    ? super VesselScope,
    ? super Void,
    ? extends R
  > delegate;

  @Override
  protected Function<Object, R> newInstance(final VesselScope scope) {
    return new Constant<>(delegate.get(scope).apply(null));
  }
}
