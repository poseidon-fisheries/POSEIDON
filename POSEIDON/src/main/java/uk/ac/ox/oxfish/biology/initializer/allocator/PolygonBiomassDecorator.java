/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import ec.util.MersenneTwisterFast;
import sim.field.geo.GeomVectorField;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;

public class PolygonBiomassDecorator implements BiomassAllocator {

    /**
     * polygons where biomass is allowed
     */
    private final GeomVectorField boundingPolygons;

    /**
     * if this is true, biomass is within the polygons. If this is false, biomass is allowed only outside of it.
     */
    private final boolean inside;

    /**
     * the delegate choosing biomass.
     */
    private final BiomassAllocator delegate;

    /**
     * pre-compute all the tiles so that you call "isInsideUnion" only once
     */
    private final HashMap<SeaTile, Boolean> insideUnion;


    public PolygonBiomassDecorator(GeomVectorField boundingPolygons, boolean inside, BiomassAllocator delegate) {
        this.boundingPolygons = boundingPolygons;
        this.inside = inside;
        this.delegate = delegate;
        boundingPolygons.computeUnion();
        insideUnion = new HashMap<>();
    }

    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {


        if (!checkIfInside(tile, map)) {
            if (inside)
                return 0;
            else
                return delegate.allocate(tile, map, random);
        } else {
            if (inside)
                return delegate.allocate(tile, map, random);
            else
                return 0;
        }

    }

    private boolean checkIfInside(SeaTile tile, NauticalMap map) {
        return
            insideUnion.computeIfAbsent(
                tile, seaTile -> boundingPolygons.isInsideUnion(map.getCoordinates(tile))
            );

    }
}
