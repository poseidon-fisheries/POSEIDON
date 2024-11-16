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

package uk.ac.ox.poseidon.agents.behaviours.destination;

import ec.util.MersenneTwisterFast;
import lombok.RequiredArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.behaviours.choices.Explorer;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;

import java.util.function.IntSupplier;

import static uk.ac.ox.poseidon.core.MasonUtils.oneOf;

@RequiredArgsConstructor
public class NeighbourhoodGridExplorer implements Explorer<Int2D> {

    private final Vessel vessel;
    private final GridPathFinder pathFinder;
    private final IntSupplier neighbourhoodSizeSupplier;
    private final MersenneTwisterFast rng;

    @Override
    public Int2D explore(final Int2D currentCell) {
        return oneOf(
            pathFinder.getAccessibleWaterNeighbours(
                currentCell == null ? vessel.getCurrentCell() : currentCell,
                neighbourhoodSizeSupplier.getAsInt()
            ),
            rng
        );
    }
}
