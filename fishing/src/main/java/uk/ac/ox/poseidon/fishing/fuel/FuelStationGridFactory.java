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

package uk.ac.ox.poseidon.fishing.fuel;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

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
 * A {@link RelativeScopeFactory} for an {@link ImmutableFuelStationGrid}
 * placing each resolved {@link FuelStation} at its own port's location in
 * {@code portGrid}. Built via {@link Factories#fuelStationGrid}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FuelStationGridFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  FuelStationGrid
> {

  private final Factory<? super S, ? extends PortGrid> portGrid;
  private final Factory<
    ? super S,
    ? extends List<? extends FuelStation>
  > fuelStations;

  @Override
  protected FuelStationGrid newInstance(final S scope) {
    final PortGrid portGrid = this.portGrid.get(scope);
    return new ImmutableFuelStationGrid(
      portGrid.getModelGrid(),
      fuelStations
        .get(scope)
        .stream()
        .collect(
          toMap(identity(), fuelStation ->
            portGrid.getLocation(fuelStation.getPort())
          )
        )
    );
  }
}
