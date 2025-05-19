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
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * Basically a transposition of the BiomassDiffuser to abundance
 * Created by carrknight on 7/7/17.
 */
public class ConstantRateAbundanceDiffuser extends AbstractAbundanceDiffuser {

    /**
     * % of differential that moves from here to there
     */
    private final double diffusingRate;


    public ConstantRateAbundanceDiffuser(
        int diffusingRange,
        double diffusingRate
    ) {
        super(diffusingRange, true);
        Preconditions.checkArgument(diffusingRate >= 0);
        Preconditions.checkArgument(diffusingRate <= 1);
        this.diffusingRate = diffusingRate;
    }

    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *
     * @param species        species moving!
     * @param here           departing point
     * @param abundanceHere  departing local biology
     * @param there          arriving point
     * @param abundanceThere arriving local biology
     * @param delta          number of fish here - number of fish there (always positive or this isn't called)
     * @param fishHere
     * @param fishThere
     * @param bin            bin/age studied
     * @param random
     * @param subdivision
     * @param biologyHere    departing local biology
     * @param biologyThere   arriving local biology
     */
    @Override
    public void move(
        Species species, SeaTile here, StructuredAbundance abundanceHere,
        SeaTile there, StructuredAbundance abundanceThere, double delta, double fishHere, double fishThere,
        int bin,
        MersenneTwisterFast random,
        boolean rounding, int subdivision,
        AbundanceLocalBiology biologyHere,
        AbundanceLocalBiology biologyThere
    ) {
        if (delta <= 0)
            return;

        double movement = delta * diffusingRate;
        if (rounding)
            movement = FishStateUtilities.randomRounding(
                delta * diffusingRate,
                random
            );

        //might be too small differential for movement
        if (movement > 0) {

            //move!
            abundanceHere.asMatrix()[subdivision][bin] -= movement;
            assert abundanceHere.asMatrix()[subdivision][bin] >= 0;
            abundanceThere.asMatrix()[subdivision][bin] += movement;
            assert abundanceThere.asMatrix()[subdivision][bin] >= 0;

        }


    }
}
