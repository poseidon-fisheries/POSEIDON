/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.geography.grids;

import lombok.*;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Envelope;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.math.DoubleMath.isMathematicalInteger;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModelGridFactory extends GlobalScopeFactory<ModelGrid> {

    private double resolutionInDegrees;
    private double westLongitude;
    private double eastLongitude;
    private double southLatitude;
    private double northLatitude;

    @Override
    protected ModelGrid newInstance(final @NonNull Simulation simulation) {
        final double widthInDegrees = eastLongitude - westLongitude;
        final double heightInDegrees = northLatitude - southLatitude;
        final int gridWidth = validateDimension("Width", widthInDegrees, resolutionInDegrees);
        final int gridHeight = validateDimension("Height", heightInDegrees, resolutionInDegrees);
        return ModelGrid.withAllCellsActive(
            gridWidth,
            gridHeight,
            new Envelope(
                westLongitude,
                eastLongitude,
                southLatitude,
                northLatitude
            )
        );
    }

    private int validateDimension(
        final String dimensionName,
        final double dimensionSize,
        final double resolution
    ) {
        final double gridDimension = dimensionSize / resolution;
        checkState(
            isMathematicalInteger(gridDimension),
            String.format(
                "%s must be an exact multiple of the resolution. " +
                    "%s: %s degrees, Resolution: %s degrees.",
                dimensionName, dimensionName, dimensionSize, resolution
            )
        );
        return (int) gridDimension;
    }
}
