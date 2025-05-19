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
 * This is the opposite of {@link Anarchy}: nothing is allowed.
 * Useless by itself, but meant to be wrapped be other classes,
 * e.g. {@link TemporaryRegulation} to implement an arbitrary pause.
 */
public class NoFishing implements Regulation {
    @Override
    public boolean canFishHere(Fisher agent, SeaTile tile, FishState model, int timeStep) {
        return false;
    }

    @Override
    public double maximumBiomassSellable(Fisher agent, Species species, FishState model, int timeStep) {
        return 0;
    }

    @Override
    public boolean allowedAtSea(Fisher fisher, FishState model, int timeStep) {
        return false;
    }

    @Override
    public Regulation makeCopy() {
        return new NoFishing();
    }
}
