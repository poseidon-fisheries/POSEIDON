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

package uk.ac.ox.poseidon.fishing.market;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

/**
 * A {@link RelativeScopeFactory} counterpart of {@link ImmutableMarketGrid},
 * placing each resolved {@link Market} at its own port's location in
 * {@link #portGrid}, built via {@link Factories#marketGrid}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class MarketGridFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  MarketGrid
> {

  private final Factory<? super S, ? extends PortGrid> portGrid;
  private final Factory<? super S, ? extends List<? extends Market>> markets;

  @Override
  protected MarketGrid newInstance(final S scope) {
    final PortGrid portGrid = this.portGrid.get(scope);
    return new ImmutableMarketGrid(
      portGrid.getModelGrid(),
      markets
        .get(scope)
        .stream()
        // An ordered map: markets hash by identity, so a HashMap's order would
        // change from run to run with the same seed
        .collect(
          toImmutableMap(identity(), market ->
            portGrid.getLocation(market.getPort())
          )
        )
    );
  }
}
