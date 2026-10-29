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

package uk.ac.ox.poseidon.geography.ports;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.MappingFactory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.functions.WithoutContext;
import uk.ac.ox.poseidon.core.utils.Pair;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.Cell;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

/**
 * A {@link RelativeScopeFactory} for one (port, coordinate) pair per cell of a
 * resolved collection, in its iteration order, for models that give their
 * ports as cells. Each port's code is what the code mapping gives for its cell,
 * called once per cell, and its name is {@code "Port "} followed by the code;
 * its coordinate is the centre of the cell. A null code is an error, and so are
 * two cells with the same code. There's no separate plain component class
 * here: the produced {@link List} is returned as-is, so this factory carries
 * the behavior doc directly. Built via
 * {@link Factories#portsFromCells(Factory, Factory, MappingFactory)} in this
 * package; the pairs are typically passed on to
 * {@link Factories#portGrid(Factory, Factory, Factory)}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class PortsFromCellsFactory<S extends Scope> extends RelativeScopeFactory<
  S,
  List<Pair<Port, Coordinate>>
> {

  private final Factory<? super S, ? extends Collection<Cell>> cells;
  private final Factory<? super S, ? extends ModelGrid> modelGrid;
  private final MappingFactory<
    ? super S,
    ? super Void,
    ? super Cell,
    ? extends String
  > code;

  @Override
  protected List<Pair<Port, Coordinate>> newInstance(final S scope) {
    final ModelGrid modelGrid = this.modelGrid.get(scope);
    final Function<Cell, String> code = WithoutContext.function(
      this.code.get(scope)
    );
    final ImmutableMap<String, Cell> cellsByCode = cells
      .get(scope)
      .stream()
      .collect(
        toImmutableMap(
          cell -> checkNotNull(code.apply(cell), "No port code for %s.", cell),
          cell -> cell
        )
      );
    return cellsByCode
      .entrySet()
      .stream()
      .map(entry ->
        Pair.of(
          new Port(entry.getKey(), "Port " + entry.getKey()),
          modelGrid.toCoordinate(entry.getValue())
        )
      )
      .toList();
  }
}
