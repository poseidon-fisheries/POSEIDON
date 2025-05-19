/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;

/**
 * Ages the abundance-based biomass.
 * Created by carrknight on 7/6/17.
 */
public interface AgingProcess {


    /**
     * called after the aging process has been constructed but before it is run.
     *
     * @param species
     */
    public void start(Species species);


    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param localBiology   list of local biologies to age
     * @param model          link to the model
     * @param rounding       whether we expect numbers to be rounded to integers
     * @param daysToSimulate simulation days
     */
    public void age(
        Collection<AbundanceLocalBiology> localBiology, Species species,
        FishState model, boolean rounding, int daysToSimulate
    );


}
