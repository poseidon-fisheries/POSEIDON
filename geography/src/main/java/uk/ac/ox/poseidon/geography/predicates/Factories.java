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

package uk.ac.ox.poseidon.geography.predicates;

import org.locationtech.jts.geom.Geometry;
import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Envelope;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;

import java.util.Collection;
import java.util.function.Predicate;

import static uk.ac.ox.poseidon.core.predicates.Factories.condition;
import static uk.ac.ox.poseidon.core.predicates.comparable.Factories.between;
import static uk.ac.ox.poseidon.geography.grids.extractors.Factories.cellValue;

/**
 * Factories for spatial predicates: testing grid cells or coordinates against bathymetry, a
 * bounding box, or a set of geometries.
 */
public class Factories {
    private Factories() {
    }

    /**
     * @param bathymetricGrid factory for the grid to test cells against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
     * {@link IsActiveWaterCell}
     * @see IsActiveWaterCell
     */
    public static <S extends Scope> IsActiveWaterCellFactory<S> isActiveWaterCell(
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid
    ) {
        return new IsActiveWaterCellFactory<>(bathymetricGrid);
    }

    /**
     * @param bathymetricGrid factory for the grid to read cell depth from
     * @param minimumDepth    the minimum depth (in the same units as the grid, positive downward)
     * @param maximumDepth    the maximum depth
     * @return a {@link Factory} for a predicate testing whether a cell's depth falls within
     * {@code [minimumDepth, maximumDepth]}
     */
    public static <S extends Scope> Factory<S, ? extends Predicate<Int2D>> inDepthRange(
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final double minimumDepth,
        final double maximumDepth
    ) {
        return condition(
            cellValue(bathymetricGrid),
            between(-maximumDepth, -minimumDepth)
        );
    }

    /**
     * @param envelope factory for the bounding box to test coordinates against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an
     * {@link InRectangularArea}
     * @see InRectangularArea
     */
    public static <S extends Scope> InRectangularAreaFactory<S> inRectangularArea(
        final Factory<? super S, ? extends Envelope> envelope
    ) {
        return new InRectangularAreaFactory<>(envelope);
    }

    /**
     * @param geometries factory for the geometries to test coordinates against
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an {@link InGeometries}
     * @see InGeometries
     */
    public static <S extends Scope> InGeometriesFactory<S> inGeometries(
        final Factory<? super S, ? extends Collection<? extends Geometry>> geometries
    ) {
        return new InGeometriesFactory<>(geometries);
    }

}
