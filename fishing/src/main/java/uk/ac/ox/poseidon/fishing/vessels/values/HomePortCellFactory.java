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

import java.util.function.Function;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;

/**
 * A {@link GlobalScopeFactory} counterpart of {@link HomePortCell}, built via
 * {@link Factories#homePortCell()}.
 */
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class HomePortCellFactory
  extends GlobalScopeFactory<Function<Vessel, Int2D>>
  implements ValueFactory<Scope, Vessel, Int2D>
{

  @Override
  protected Function<Vessel, Int2D> newInstance(final Scope scope) {
    return new HomePortCell();
  }
}
