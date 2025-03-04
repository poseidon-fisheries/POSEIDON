/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.regulations.predicates.spatial;

import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.Action;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.function.Predicate;

@RequiredArgsConstructor
public class ActionCellPredicate implements Predicate<Action> {

    private final ModelGrid modelGrid;
    private final Predicate<Int2D> cellPredicate;

    @Override
    public boolean test(final Action action) {
        return cellPredicate.test(modelGrid.toCell(action.getStartCoordinate())) ||
            cellPredicate.test(modelGrid.toCell(action.getEndCoordinate()));
    }
}
