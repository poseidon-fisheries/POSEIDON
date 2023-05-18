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

import uk.ac.ox.oxfish.biology.Species;

/**
 * filters any fish above/below a given cutoff length
 * Created by carrknight on 3/11/16.
 */
public class CutoffAbundanceFilter extends FormulaAbundanceFilter {


    private final double cutoffLevel;

    private final boolean selectHigherThanCutoff;


    public CutoffAbundanceFilter(
        double cutoffLevel, boolean selectHigherThanCutoff,
        final boolean rounding
    ) {
        super(false, rounding);
        this.cutoffLevel = cutoffLevel;
        this.selectHigherThanCutoff = selectHigherThanCutoff;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected double[][] computeSelectivity(Species species) {
        double[][] toReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];


        double higherThanCutoff = selectHigherThanCutoff ? 1 : 0;

        for (int cohort = 0; cohort < species.getNumberOfSubdivisions(); cohort++)
            for (int age = 0; age < species.getNumberOfBins(); age++) {
                toReturn[cohort][age] = species.getLength(cohort,
                    age) >= cutoffLevel ? higherThanCutoff : 1 - higherThanCutoff;

            }
        return toReturn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CutoffAbundanceFilter that = (CutoffAbundanceFilter) o;

        if (Double.compare(that.cutoffLevel, cutoffLevel) != 0) return false;
        return selectHigherThanCutoff == that.selectHigherThanCutoff;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(cutoffLevel);
        result = (int) (temp ^ (temp >>> 32));
        result = 31 * result + (selectHigherThanCutoff ? 1 : 0);
        return result;
    }
}
