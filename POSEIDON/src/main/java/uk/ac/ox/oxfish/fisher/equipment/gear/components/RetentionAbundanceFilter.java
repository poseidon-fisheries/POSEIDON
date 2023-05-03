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

import java.util.Objects;

/**
 * The retention filter formula that appears the most in the spreadhseets
 * (including thornyheads and sablefish)
 * Created by carrknight on 3/9/16.
 */
public class RetentionAbundanceFilter extends FormulaAbundanceFilter {

    private final double inflection;

    private final double slope;

    private final double asymptote;



    public RetentionAbundanceFilter(
            boolean memoization, double inflection, double slope, double asymptote, final boolean rounding) {
        super(memoization, rounding);
        this.inflection = inflection;
        this.slope = slope;
        this.asymptote = asymptote;
    }

    @Override
    protected double[][] computeSelectivity(Species species)
    {
        double[][] toReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for(int subdivision = 0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
            for(int age=0; age<species.getNumberOfBins(); age++)
            {
                toReturn[subdivision][age] =
                        asymptote/(1+Math.exp(-( species.getLength(subdivision,age)-inflection)/slope));


            }
        return toReturn;

    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RetentionAbundanceFilter that = (RetentionAbundanceFilter) o;

        if (Double.compare(that.inflection, inflection) != 0) return false;
        if (Double.compare(that.slope, slope) != 0) return false;
        return Double.compare(that.asymptote, asymptote) == 0;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(inflection);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(slope);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(asymptote);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
