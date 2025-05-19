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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.Startable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * basically a CatchSample that keeps track of which boats to observe
 */
public interface CatchAtLengthSampler extends Startable {

    /**
     * Step to call (from the outside!) to tell the sampler to look at this day's data
     * Because fishers store their landings in weight, we need a function to turn them back into abundance. Here
     * we use the REAL weight function to do so
     */
    void observeDaily();

    /**
     * when we need to zero the abundance array, call this.
     */
    void resetCatchObservations();

    /**
     * get approximate NUMBER of fish recorded as caught
     */
    double[][] getAbundance();

    /**
     * get approximate NUMBER of fish recorded as caught using a custom function matching bin weight to numbers
     * (this is useful if we don't want to use the REAL species parameters)
     */
    double[][] getAbundance(Function<Map.Entry<Integer, Integer>, Double> subdivisionBinToWeightFunction);


    Species getSpecies();

    double[][] getLandings();

    /**
     * returns unmodifiable list showing fishers
     *
     * @return
     */
    List<Fisher> viewObservedFishers();

}
