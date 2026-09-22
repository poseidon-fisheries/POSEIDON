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

package uk.ac.ox.poseidon.agents.fisheables;

import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.biology.FisheableGrid;
import uk.ac.ox.poseidon.core.Factory;

/**
 * Factories for suppliers of the {@link uk.ac.ox.poseidon.biology.Fisheable} at a vessel's current
 * location.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory} for a
     * {@link CurrentCellFisheable}
     * @see CurrentCellFisheableFactory
     */
    public static CurrentCellFisheableFactory currentCellFisheable(
        final Factory<? super VesselScope, ? extends FisheableGrid> fisheableGrid
    ) {
        return new CurrentCellFisheableFactory(fisheableGrid);
    }
}
