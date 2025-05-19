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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * like constant rate abundance diffuser, but only works for a certain age range (outside this range fish is immobile!)
 * <p>
 * Created by carrknight on 7/7/17.
 */
public class AgeLimitedConstantRateDiffuser extends ConstantRateAbundanceDiffuser {


    /**
     * fish of this age or above can move
     */
    private final int minMovementAge;

    /**
     * fish of this age or below can move
     */
    private final int maxMovementAge;


    public AgeLimitedConstantRateDiffuser(
        int diffusingRange, double diffusingRate,
        int minMovementAge, int maxMovementAge
    ) {
        super(diffusingRange, diffusingRate);
        this.minMovementAge = minMovementAge;
        this.maxMovementAge = maxMovementAge;
    }

    /**
     * ask implementation how to move. This gets called iff there is a positive delta (that is, there are more fish here than there)
     *
     * @param species
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
        Species species,
        SeaTile here, StructuredAbundance abundanceHere,
        SeaTile there,
        StructuredAbundance abundanceThere,
        double delta, double fishHere, double fishThere, int bin,
        MersenneTwisterFast random,
        boolean rounding, int subdivision,
        AbundanceLocalBiology biologyHere,
        AbundanceLocalBiology biologyThere
    ) {

        if (delta <= 0)
            return;
        if (bin >= minMovementAge && bin <= maxMovementAge)
            super.move(species,
                here,
                abundanceHere,
                there,
                abundanceThere,
                delta,
                fishHere,
                fishThere,
                bin,
                random,
                rounding,
                subdivision,
                biologyHere,
                biologyThere
            );
    }
}
