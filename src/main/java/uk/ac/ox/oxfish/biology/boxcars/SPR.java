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

import java.util.function.Function;

public class SPR {



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
            Function<Integer,Double> weightAtAgeFunction,
            Function<Integer,Double> maturityAtAgeFunction

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
        int totalCount =0;
        double[] count = new double[bins];
        for(int bin=0; bin< abundance.getBins(); bin++) {
            for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++)
            {

                double abundanceHere = abundance.getAbundance(subdivision, bin);
                if(abundanceHere>0) {
                    int countBin = (int) Math.round(species.getLength(subdivision, bin) / lengthBinCm);
                    if(countBin>=count.length)
                    {
                        //we could be using a bad or simplified lengthInfinity
                        assert species.getLength(subdivision,bin)>=lengthInfinity;
                        countBin = count.length-1;
                    }
                    count[countBin]+=
                            abundanceHere;
                    totalCount+=abundanceHere;
                }

            }
        }
        if(totalCount==0)
            return Double.NaN;
        int mostFrequentBin = 0;
        for(int i=1; i<count.length; i++)
            mostFrequentBin = count[i]>count[mostFrequentBin] ? i : mostFrequentBin;


        //get the most frequent length (with respect to the 5cm bins, not the original species structure)
        double mostFrequentLength = mostFrequentBin*lengthBinCm;
        //get average length for all fish above mostFrequentLength
        double sumLengthAboveThreshold = 0;
        double sumAbundanceAbovethreshold = 0;
        //also get the minimum length at which a catch occurred
        double minimumLengthCaught = Double.MAX_VALUE;
        for(int bin=0; bin< abundance.getBins(); bin++) {
            for (int subdivision = 0; subdivision < abundance.getSubdivisions(); subdivision++) {
                double currentAbundance = abundance.getAbundance(subdivision, bin);
                if(currentAbundance > 0)
                {
                    double currentLength = species.getLength(subdivision, bin);
                    if (currentLength >= mostFrequentLength) {
                        sumLengthAboveThreshold += currentLength * currentAbundance;
                        sumAbundanceAbovethreshold += currentAbundance;

                    }
                    if(currentLength<minimumLengthCaught)
                        minimumLengthCaught = currentLength;
                }
            }
        }
        double meanLengthCaughtAboveThreshold = sumLengthAboveThreshold/sumAbundanceAbovethreshold;

        //guess instantaneous mortality through that
        double instantaneousMortality = (kParameter * (lengthInfinity - meanLengthCaughtAboveThreshold))/
                (meanLengthCaughtAboveThreshold - mostFrequentLength);
        instantaneousMortality= instantaneousMortality - naturalMortality;

        assert mostFrequentLength>minimumLengthCaught;
        double mortalityAtAge[] = new double[maxSimulatedAge];
        for(int age=0; age<mortalityAtAge.length; age++) {
            double lengthAtAge = species.getLengthAtAge(age+1, 0);



            mortalityAtAge[age] = lengthAtAge <= minimumLengthCaught ?
                    0 :
                    Math.min(1,(lengthAtAge-minimumLengthCaught)/
                            (mostFrequentLength-minimumLengthCaught))*instantaneousMortality;

            assert  mortalityAtAge[age] >=0;
            assert  mortalityAtAge[age] <= instantaneousMortality;

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
