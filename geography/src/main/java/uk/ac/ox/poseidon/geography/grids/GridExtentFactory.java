/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.geography.grids;

import com.vividsolutions.jts.geom.Envelope;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GridExtentFactory extends GlobalScopeFactory<GridExtent> {

    private int gridWidth;
    private int gridHeight;
    private double westLongitude;
    private double eastLongitude;
    private double southLatitude;
    private double northLatitude;

    @Override
    protected GridExtent newInstance(final Simulation simulation) {
        return new GridExtent(
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
}
