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

import java.util.Objects;

/**
 * A logistic abundance filter as for example the one used for trawl selectivity
 * for thornyheads; works on the length of the fish
 * Created by carrknight on 3/9/16.
 */
public class LogisticAbundanceFilter extends FormulaAbundanceFilter {

    private final double aParameter;

    private final double bParameter;

    private final boolean logBaseTen;


    public LogisticAbundanceFilter(double aParameter, double bParameter, boolean memoization, final boolean rounding, boolean logBaseTen) {
        super(memoization, rounding);
        this.aParameter = aParameter;
        this.bParameter = bParameter;
        this.logBaseTen = logBaseTen;
    }




    protected double[][] computeSelectivity(Species species)
    {
        double[][] toReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for(int subdivision = 0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
            for(int age=0; age<species.getNumberOfBins(); age++)
            {
                toReturn[subdivision][age] =
                        logBaseTen ?
                        1d/(1+Math.exp(-Math.log10(19)*( species.getLength(subdivision,age)-aParameter)/bParameter)) :
                        1d/(1+Math.exp(-Math.log(19)*( species.getLength(subdivision,age)-aParameter)/bParameter))

                ;


            }
        return toReturn;

    }



    /** {@inheritDoc} */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogisticAbundanceFilter that = (LogisticAbundanceFilter) o;
        return Double.compare(that.aParameter, aParameter) == 0 &&
                Double.compare(that.bParameter, bParameter) == 0 &&
                Boolean.compare(logBaseTen,that.logBaseTen) == 0 &&
                isMemoization() == that.isMemoization();
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(aParameter,bParameter,logBaseTen);
    }


    public double getaParameter() {
        return aParameter;
    }

    public double getbParameter() {
        return bParameter;
    }


}

