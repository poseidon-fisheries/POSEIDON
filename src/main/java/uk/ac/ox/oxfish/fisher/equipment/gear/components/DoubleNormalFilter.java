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
 * The selectivity filter for most species in the assesment reports
 * Created by carrknight on 3/21/16.
 */
public class DoubleNormalFilter extends FormulaAbundanceFilter{


    final private double peak;

    final private double top;

    final private double  ascWidth;

    final private double  dscWidth;

    final private double  initialScaling;

    final private double  finalScaling;

    final private double binMin;

    final private double binMax;

    final private double binWidth;


    public DoubleNormalFilter(
            boolean memoization, final boolean rounding, double peak, double top, double ascWidth, double dscWidth,
            double initialScaling,
            double finalScaling, double binMin, double binMax, double binWidth) {
        super(memoization, rounding);
        this.peak = peak;
        this.top = top;
        this.ascWidth = ascWidth;
        this.dscWidth = dscWidth;
        this.initialScaling = initialScaling;
        this.finalScaling = finalScaling;
        this.binMin = binMin;
        this.binMax = binMax;
        this.binWidth = binWidth;
    }


    public DoubleNormalFilter(
            boolean memoization, final boolean rounding, double peak, double top, double ascWidth, double dscWidth,
            double binMin,
            double binMax,
            double binWidth) {
        this(memoization, rounding, peak, top, ascWidth, dscWidth, Double.NaN, Double.NaN, binMin, binMax, binWidth);

    }

    /**
     * the method that gives the probability matrix for each age class and each sex of not filtering the abundance away
     *
     * @param species
     * @return
     */
    @Override
    protected double[][] computeSelectivity(Species species) {




        //first build the asc and desc vectors
        //also the join1 and join2
        double expWidth = Math.exp(ascWidth);
        double expDsc = Math.exp(dscWidth);
        double expTop = peak + binWidth + (0.99*  (binMax+binWidth/2) -peak - binWidth ) / (1+ Math.exp(-top));


        double[][] asc = new double[2][species.getNumberOfBins()];
        double[][] desc = new double[2][species.getNumberOfBins()];
        double[][] join1 = new double[2][species.getNumberOfBins()];
        double[][] join2 = new double[2][species.getNumberOfBins()];
        for(int age=0;age<species.getNumberOfBins();age++)
        {
            double bin =  binWidth/2 +  (species.getLength(FishStateUtilities.MALE,age) - binMin) / binWidth;
            //EXP(-(($B26-$E$7)^2/$E$9))
            asc[FishStateUtilities.MALE][age] = Math.exp(-(Math.pow(bin-peak,2)/expWidth));
            desc[FishStateUtilities.MALE][age] = Math.exp(-(Math.pow(bin-expTop,2)/expDsc));
            //1/(1+EXP(-($H$24*($B26-$E$7)/(1+ABS($B26-$E$7)))))
            join1[FishStateUtilities.MALE][age] = 1d/(1+Math.exp(-(20*(bin-peak)/(1+Math.abs(bin-peak)))));
            //1/(1+EXP(-($I$24*($B26-$E$8)/(1+ABS($B26-$E$8)))))
            join2[FishStateUtilities.MALE][age] = 1d/(1+Math.exp(-(20*(bin-expTop)/(1+Math.abs(bin-expTop)))));
            bin =  binWidth/2 +  (species.getLength(FishStateUtilities.FEMALE,age) - binMin) / binWidth;
            asc[FishStateUtilities.FEMALE][age] = Math.exp(-(Math.pow(bin-peak,2)/expWidth));
            desc[FishStateUtilities.FEMALE][age] = Math.exp(-(Math.pow(bin-expTop,2)/expDsc));
            join1[FishStateUtilities.FEMALE][age] = 1d/(1+Math.exp(-(20*(bin-peak)/(1+Math.abs(bin-peak)))));
            join2[FishStateUtilities.FEMALE][age] = 1d/(1+Math.exp(-(20*(bin-expTop)/(1+Math.abs(bin-expTop)))));

        }
        //if necessary scale the asc vector
        if(Double.isFinite(initialScaling))
        {
            double scaling = 1d/(1+Math.exp(-initialScaling));
            //EXP(-(($B20-$E$7)^2/$E$9))
            double minScaling = Math.exp(-(Math.pow(binMin + binWidth/2d-peak,2)/expWidth));
            for(int age=0;age<species.getNumberOfBins()-1;age++)
            {
                //($E$11+(1-$E$11)*(C26-$C$20)/($C$21-$C$20))
                asc[0][age] = scaling+(1-scaling)*(asc[0][age]-minScaling)/( 1- minScaling);
                asc[1][age] = scaling+(1-scaling)*(asc[1][age]-minScaling)/( 1- minScaling);
            }

        }


        //if necessary scale the desc vector
        if(Double.isFinite(finalScaling))
        {
            double scaling = 1d/(1+Math.exp(-finalScaling));
            //EXP(-(($B20-$E$7)^2/$E$9))
            double maxScaling = Math.exp(-(Math.pow(binMax + binWidth/2d-expTop,2)/expDsc));
            for(int age=0;age<species.getNumberOfBins()-1;age++)
            {
                //((1+($E$12-1)*(E26-$C$22)/($C$23-$C$22)),E26)
                desc[0][age] = 1 + (scaling-1)*(desc[0][age]-1)/(maxScaling-1);
                desc[1][age] = 1 + (scaling-1)*(desc[1][age]-1)/(maxScaling-1);
            }

        }



        //now turn it into selectivity thank god
        double[][] selex = new double[2][species.getNumberOfBins()];
        for(int age=0;age<species.getNumberOfBins();age++)
        {

            if(Double.isNaN(initialScaling) ||
                    species.getLength(FishStateUtilities.MALE,age)>-1000-initialScaling)
            {
                //(D26*(1-G26)+G26*(1*(1-H26)+F26*H26))
                selex[FishStateUtilities.MALE][age] =
                        asc[0][age]*(1-join1[FishStateUtilities.MALE][age])+
                                join1[FishStateUtilities.MALE][age]*((1-join2[FishStateUtilities.MALE][age])+
                                        desc[FishStateUtilities.MALE][age]*join2[0][age]);
            }
            else
            {
                selex[FishStateUtilities.MALE][age] = 0;
            }

            if(Double.isNaN(initialScaling) ||
                    -1000-initialScaling> species.getLength(FishStateUtilities.FEMALE,age))
            {
                //(D26*(1-G26)+G26*(1*(1-H26)+F26*H26))
                selex[FishStateUtilities.FEMALE][age] =
                        asc[FishStateUtilities.FEMALE][age]*(1-join1[FishStateUtilities.FEMALE][age])+
                                join1[FishStateUtilities.FEMALE][age]*
                                        ((1-join2[FishStateUtilities.FEMALE][age])+
                                                desc[FishStateUtilities.FEMALE][age]*
                                                        join2[FishStateUtilities.FEMALE][age]);
            }
            else
            {
                selex[FishStateUtilities.FEMALE][age] = 0;
            }


        }
        return selex;


    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DoubleNormalFilter that = (DoubleNormalFilter) o;
        return Double.compare(that.peak, peak) == 0 &&
                Double.compare(that.top, top) == 0 &&
                Double.compare(that.ascWidth, ascWidth) == 0 &&
                Double.compare(that.dscWidth, dscWidth) == 0 &&
                Double.compare(that.initialScaling, initialScaling) == 0 &&
                Double.compare(that.finalScaling, finalScaling) == 0 &&
                Double.compare(that.binMin, binMin) == 0 &&
                Double.compare(that.binMax, binMax) == 0 &&
                Double.compare(that.binWidth, binWidth) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(peak, top, ascWidth, dscWidth, initialScaling, finalScaling, binMin, binMax, binWidth);
    }

    public double getPeak() {
        return peak;
    }

    public double getTop() {
        return top;
    }

    public double getAscWidth() {
        return ascWidth;
    }

    public double getDscWidth() {
        return dscWidth;
    }

    public double getInitialScaling() {
        return initialScaling;
    }

    public double getFinalScaling() {
        return finalScaling;
    }

    public double getBinMin() {
        return binMin;
    }

    public double getBinMax() {
        return binMax;
    }

    public double getBinWidth() {
        return binWidth;
    }
}
