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

package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A regulation that enforces a single, specific protected area.
 * <p>
 * Stores the protected status of each tile in the {@code inArea} array for speed of access.
 * The array is not meant to ever be modified. If MPAs need to change during a simulation,
 * a new SpecificProtectedArea should be created.
 */
public class SpecificProtectedArea implements Regulation {

    private final boolean[][] inArea;
    private final String name;

    public SpecificProtectedArea(final boolean[][] inArea, String name) {
        this.inArea = inArea.clone();
        this.name = name;
    }

    public boolean[][] getInAreaArrayClone() {
        return inArea.clone();
    }

    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {
        return !isProtected(tile);
    }

    public boolean isProtected(SeaTile tile) {
        return inArea[tile.getGridX()][tile.getGridY()];
    }

    @Override
    public double maximumBiomassSellable(
        Fisher agent,
        Species species,
        FishState model,
        int timeStep
    ) {
        return Double.MAX_VALUE;
    }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        return true;
    }

    @Override
    public Regulation makeCopy() {
        return new SpecificProtectedArea(inArea, name);
    }

    @Override
    public String toString() {
        return "SpecificProtectedArea{" +
            "name='" + name + '\'' +
            '}';
    }
}
