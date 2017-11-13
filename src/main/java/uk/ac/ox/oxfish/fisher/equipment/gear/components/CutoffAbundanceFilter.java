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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

/**
 * filters any fish above/below a given cutoff length
 * Created by carrknight on 3/11/16.
 */
public class CutoffAbundanceFilter extends FormulaAbundanceFilter {


    private final double cutoffLevel;

    private final boolean selectHigherThanCutoff;


    public CutoffAbundanceFilter(double cutoffLevel, boolean selectHigherThanCutoff,
                                 final boolean rounding) {
        super(false, rounding);
        this.cutoffLevel = cutoffLevel;
        this.selectHigherThanCutoff = selectHigherThanCutoff;
    }

    /** {@inheritDoc} */
    @Override
    protected double[][] computeSelectivity(Species species) {
        double[][] toReturn = new double[2][species.getMaxAge()+1];
        ImmutableList<Double> maleLength = species.getLengthMaleInCm();
        ImmutableList<Double> femaleLength = species.getLengthFemaleInCm();

        double higherThanCutoff = selectHigherThanCutoff ? 1 : 0;

        for(int age=0; age<species.getMaxAge()+1; age++)
        {
            toReturn[FishStateUtilities.MALE][age] = maleLength.get(age)>=cutoffLevel ? higherThanCutoff : 1-higherThanCutoff;


            toReturn[FishStateUtilities.FEMALE][age] =
                    femaleLength.get(age)>=cutoffLevel ? higherThanCutoff : 1-higherThanCutoff;

        }
        return toReturn;
    }
}
