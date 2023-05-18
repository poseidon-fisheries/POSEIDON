/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalObservation;
import uk.ac.ox.oxfish.fisher.heatmap.regression.numerical.GeographicalRegression;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.nio.file.Path;

public class SmoothFileBiomassAllocator extends FileBiomassAllocator {


    private final GeographicalRegression<Double> smoother;

    public SmoothFileBiomassAllocator(
        final Path csvFile, final boolean inputFileHasHeader,
        final GeographicalRegression<Double> smoother
    ) {
        super(csvFile, inputFileHasHeader);
        this.smoother = smoother;
    }

    @Override
    protected void observePoint(
        final NauticalMap map, final Double currentX, final Double currentY, final Double nextValue
    ) {

        smoother.addObservation(
            new GeographicalObservation<Double>(
                map.getSeaTile(new Coordinate(currentX, currentY)), 0,
                nextValue


            ), null, null);


    }

    @Override
    protected double allocateNumerically(
        final SeaTile tile, final NauticalMap map, final MersenneTwisterFast random
    ) {
        return smoother.predict(tile, 0, null, null);
    }
}
