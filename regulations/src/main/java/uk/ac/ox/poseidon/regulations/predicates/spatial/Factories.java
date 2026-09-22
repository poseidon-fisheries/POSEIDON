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

package uk.ac.ox.poseidon.regulations.predicates.spatial;

import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;

import java.util.function.Predicate;

/**
 * Factories for predicates over a {@link uk.ac.ox.poseidon.regulations.SpatialAction}'s start/end
 * location, either as grid cells or as raw coordinates.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
     * {@link ActionCellPredicate}
     * @see ActionCellPredicateFactory
     */
    public static <S extends Scope> ActionCellPredicateFactory<S> actionCellPredicate(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Predicate<Int2D>> cellPredicate
    ) {
        return new ActionCellPredicateFactory<>(modelGrid, cellPredicate);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
     * {@link ActionCoordinatePredicate}
     * @see ActionCoordinatePredicateFactory
     */
    public static <S extends Scope> ActionCoordinatePredicateFactory<S> actionCoordinatePredicate(
        final Factory<? super S, ? extends Predicate<Coordinate>> coordinatePredicate
    ) {
        return new ActionCoordinatePredicateFactory<>(coordinatePredicate);
    }

}
