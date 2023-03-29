/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 * A fixed proportion of fish moves from one bin to the next each step
 * Created by carrknight on 7/6/17.
 */
public class ProportionalAgingProcess extends LocalAgingProcess {


    /**
     * generates a number between 0 and 1 (the method bounds it so otherwise) representing
     * how many fish of class x move between one bin and the next
     */
    private final DoubleParameter proportionAging;

    public ProportionalAgingProcess(final DoubleParameter proportionAging) {
        this.proportionAging = proportionAging;
    }

    /**
     * ignored
     */
    @Override
    public void start(final Species species) {
        //ignored
    }

    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param localBiology
     * @param species
     * @param model
     * @param rounding
     * @param daysToSimulate
     */
    @Override
    public void ageLocally(
        final AbundanceLocalBiology localBiology, final Species species, final FishState model, final boolean rounding,
        final int daysToSimulate
    ) {

        final StructuredAbundance abundance = localBiology.getAbundance(species);


        for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++)
        //go from oldest to youngest and age them (to avoid double aging)
        {
            final double[] cohort = abundance.asMatrix()[subdivision];
            for (int bin = cohort.length - 1; bin >= 0; bin--) {
                //male
                double deltaMale = proportionalStep(cohort[bin], model.getRandom(), daysToSimulate / 365d);
                if (rounding)
                    deltaMale = (int) deltaMale;
                cohort[bin] -= deltaMale;
                assert cohort[bin] >= 0;
                if (bin < cohort.length - 1) //if you are at very last bin, you just die
                    cohort[bin + 1] += deltaMale;
            }
        }


    }


    /**
     * tells you for these many fish how many age and how many don't
     *
     * @param binAbundance the number of fish
     * @param scaling
     * @return fish that move to the next bin
     */
    private double proportionalStep(final double binAbundance, final MersenneTwisterFast random, final double scaling) {

        Preconditions.checkArgument(binAbundance >= 0);
        if (binAbundance == 0)
            return 0;
        final double proportion = Math.max(0, Math.min(1, proportionAging.applyAsDouble(random))) * scaling;
        return (proportion * binAbundance);

    }
}
