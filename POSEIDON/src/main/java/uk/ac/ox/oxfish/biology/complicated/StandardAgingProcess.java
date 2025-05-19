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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Simple aging structure where each cohort is aged by 1;
 * Basically this makes sense if the abundance is split really into ages rather than
 * other forms of bins
 * Created by carrknight on 7/6/17.
 */
public class StandardAgingProcess extends LocalAgingProcess {


    /**
     * if this is false, last year fish dies off. Otherwise it accumulates in the last bin
     */
    final boolean preserveLastAge;

    public StandardAgingProcess(boolean preserveLastAge) {
        this.preserveLastAge = preserveLastAge;
    }

    /**
     * called after the aging process has been initialized but before it is run.
     *
     * @param species
     */
    @Override
    public void start(Species species) {

    }

    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param localBiology
     * @param model
     * @param rounding
     * @param daysToSimulate
     */
    @Override
    public void ageLocally(
        AbundanceLocalBiology localBiology, Species species,
        FishState model, boolean rounding, int daysToSimulate
    ) {

        Preconditions.checkArgument(daysToSimulate == 365, "This should be used yearly!");
        //get the age structure (these are not copies!)
        StructuredAbundance abundance = localBiology.getAbundance(species);
        //escalator move everything
        for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++) {
            double[] segment = abundance.asMatrix()[subdivision];
            double oldest = segment[segment.length - 1];
            System.arraycopy(segment, 0, segment, 1, segment.length - 1);
            segment[0] = 0;
            if (preserveLastAge)
                segment[segment.length - 1] += oldest;

        }
    }


    /**
     * Getter for property 'preserveLastAge'.
     *
     * @return Value for property 'preserveLastAge'.
     */
    public boolean isPreserveLastAge() {
        return preserveLastAge;
    }
}
