/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.function.Function;

public class SPR implements SPRFormula{


    private final boolean removeSmallestPercentile;

    public SPR(boolean removeSmallestPercentile) {
        this.removeSmallestPercentile = removeSmallestPercentile;
    }

    @Override
    public double computeSPR(SPRAgent sprAgent, StructuredAbundance abundance) {
        return SPR.computeSPR(
                abundance,
                sprAgent.getSpecies(),
                sprAgent.getAssumedNaturalMortality(),
                sprAgent.getAssumedKParameter(),
                sprAgent.getAssumedLinf(),
                100,
                1000d,
                sprAgent.getAssumedLengthBinCm(),
                sprAgent.getAgeToWeightFunction(),
                sprAgent.getAgeToMaturityFunction(),
                removeSmallestPercentile
        );
    }

    public static double simulateVirginSpawningBiomassForBoxcarMethod(){
        throw new RuntimeException("to be done!");
    }


    public static double computeSPR(

            StructuredAbundance abundance,
            Species species,
            double naturalMortality,
            double kParameter,
            double lengthInfinity,
            int maxSimulatedAge, //default = 100
            double initialVirginPopulation, //default 1000
            double lengthBinCm,
            Function<Integer, Double> weightAtAgeFunction,
            Function<Integer, Double> maturityAtAgeFunction,
            //tnc formula actually removes the smallest percentile.
            //this is actually quite useful in POSEIDON because we deal with fractional rather than real counts, most of the time!
            boolean removeSmallestPercentile

    )
    {

        //generate a fake distribution assuming only natural mortality
        double virginSpawningBiomass = 0d;
        double[] virginAbundance = new double[maxSimulatedAge];
        virginAbundance[0] = initialVirginPopulation;
        //for each age assume all that happened is natural mortality
        for(int age=1; age<virginAbundance.length; age++)
        {
            virginAbundance[age]= initialVirginPopulation *
                    Math.exp(-naturalMortality * (age));

            //todo these arguments are age+1 in Peter's code
            double biomassAtAge = virginAbundance[age] * weightAtAgeFunction.apply(age);
            virginSpawningBiomass+= biomassAtAge * maturityAtAgeFunction.apply(age);
        }


        double maxLength =  species.getLengthAtAge(Integer.MAX_VALUE,0);
        int bins =  (int)Math.ceil(maxLength / lengthBinCm) + 1;
        //find most frequent length!
        CatchAtLength catchAtLength = new CatchAtLength(
                abundance,
                species,
                lengthBinCm,
                bins
        );

        if(catchAtLength.getTotalCount()==0)
            return Double.NaN;
        int mostFrequentBin = 0;

        final double[] catchAtLengthArray = catchAtLength.getCatchAtLength();
        for(int i = 1; i< catchAtLengthArray.length; i++)
            mostFrequentBin = catchAtLengthArray[i]>catchAtLengthArray[mostFrequentBin] ? i : mostFrequentBin;


        //get the most frequent length (with respect to the 5cm bins, not the original species structure)
        double mostFrequentLength = mostFrequentBin*lengthBinCm ;
        //get average length for all fish above mostFrequentLength
        double sumLengthAboveThreshold = 0;
        double sumAbundanceAbovethreshold = 0;

        //quantile adjustments!
        double adjustmentThreshold = removeSmallestPercentile ? catchAtLength.getTotalCount() * 0.01 : 0d;


        //also get the minimum length at which a catch occurred
        double minimumLengthCaught = Double.MAX_VALUE;
        for(int bin=0; bin< abundance.getBins(); bin++) {
            double currentlyCounted = 0;
            for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++) {
                double currentAbundance = abundance.getAbundance(subdivision, bin);
                currentlyCounted += currentAbundance;
                if(currentAbundance > 0)
                {
                    double currentLength = species.getLength(subdivision, bin);
                    if (currentLength >= mostFrequentLength) {
                        sumLengthAboveThreshold += currentLength * currentAbundance;
                        sumAbundanceAbovethreshold += currentAbundance;

                    }
                    if(currentlyCounted > adjustmentThreshold && currentLength<minimumLengthCaught)
                        minimumLengthCaught = currentLength;
                }
            }
        }
        double meanLengthCaughtAboveThreshold = sumLengthAboveThreshold/sumAbundanceAbovethreshold;

        //guess instantaneous mortality through that
        double instantaneousMortality = (kParameter * (lengthInfinity - meanLengthCaughtAboveThreshold))/
                (meanLengthCaughtAboveThreshold - mostFrequentLength);
        instantaneousMortality= instantaneousMortality - naturalMortality;
        //round it to 0 if the instantaneous mortality goes below
        if(instantaneousMortality<0)
            instantaneousMortality = 0;

        assert mostFrequentLength>minimumLengthCaught;
        double mortalityAtAge[] = new double[maxSimulatedAge];
        for(int age=0; age<mortalityAtAge.length; age++) {
            double lengthAtAge = species.getLengthAtAge(age+1, 0);



            mortalityAtAge[age] = lengthAtAge <= minimumLengthCaught ?
                    0 :
                    Math.min(1,(lengthAtAge-minimumLengthCaught)/
                            (mostFrequentLength-minimumLengthCaught))*instantaneousMortality;

            assert  mortalityAtAge[age] >= -FishStateUtilities.EPSILON : mortalityAtAge[age];
            assert  mortalityAtAge[age] <= instantaneousMortality + FishStateUtilities.EPSILON;

        }

        double[] guessedAbundance = new double[maxSimulatedAge];
        guessedAbundance[0] = initialVirginPopulation;
        //for each age assume all that happened is natural mortality
        double simulatedSpawningBiomass = 0;
        for(int age=1; age<virginAbundance.length; age++)
        {
            guessedAbundance[age]= guessedAbundance[age-1] *
                    Math.exp(-mortalityAtAge[age-1] -naturalMortality);

            //todo these arguments are age+1 in Peter's code
            double biomassAtAge = guessedAbundance[age] * weightAtAgeFunction.apply(age);
            simulatedSpawningBiomass += biomassAtAge * maturityAtAgeFunction.apply(age);
        }

        return simulatedSpawningBiomass/virginSpawningBiomass;



    }

}
