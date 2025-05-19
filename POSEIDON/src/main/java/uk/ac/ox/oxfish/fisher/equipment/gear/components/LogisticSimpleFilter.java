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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * logistic selectivity with formula:
 * 1/(1+exp(selex_1-selex_2*L))
 * which hasn't the log10 correction from Stock assessments
 */
public class LogisticSimpleFilter extends FormulaAbundanceFilter {

    private final double selex1;

    private final double selex2;


    public LogisticSimpleFilter(boolean memoization, boolean rounding, double selex1, double selex2) {
        super(memoization, rounding);
        this.selex1 = selex1;
        this.selex2 = selex2;
    }

    @Override
    protected double[][] computeSelectivity(Species species) {
        double[][] toReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for (int subdivision = 0; subdivision < species.getNumberOfSubdivisions(); subdivision++)
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                toReturn[subdivision][bin] = 1 / (1 + Math.exp(selex1 - selex2 * species.getLength(subdivision, bin)));


            }
        return toReturn;
    }

    public double getSelex1() {
        return selex1;
    }

    public double getSelex2() {
        return selex2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LogisticSimpleFilter that = (LogisticSimpleFilter) o;

        if (Double.compare(that.selex1, selex1) != 0) return false;
        return Double.compare(that.selex2, selex2) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(selex1);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(selex2);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
