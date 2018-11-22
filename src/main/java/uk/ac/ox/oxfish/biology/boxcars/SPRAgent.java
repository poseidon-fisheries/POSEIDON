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

import com.google.common.annotations.VisibleForTesting;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class SPRAgent implements AdditionalStartable {


    private final String surveyTag;

    /**
     * object that returns true whenever the fisher is to be sampled for this SPR computation
     */
    private final Predicate<Fisher> samplingSelector;

    private final Species species;

    private final double assumedLinf;


    private final double assumedKParameter;

    private final double assumedNaturalMortality;

    private final int assumedMaxAge;

    private final double assumedVirginRecruits;

    private final double assumedLengthBinCm;

    private final List<Fisher> sampledFishers = new LinkedList<>();

    private final double assumedVarA;

    private final double assumedVarB;

    private final double assumedLenghtAtMaturity;

    /**
     * here we keep the COUNT of fish that we are tracking!
     */
    private double[][] abundance;

    public SPRAgent(
            String surveyTag, Species species,
            Predicate<Fisher> samplingSelector,
            double assumedLinf,
            double assumedKParameter, double assumedNaturalMortality,
            int assumedMaxAge,
            double assumedVirginRecruits,
            double assumedLengthBinCm, double assumedVarA, double assumedVarB, double assumedLenghtAtMaturity) {
        this.samplingSelector = samplingSelector;
        this.surveyTag = surveyTag;
        this.species = species;
        this.assumedLinf = assumedLinf;
        this.assumedKParameter = assumedKParameter;
        this.assumedNaturalMortality = assumedNaturalMortality;
        this.assumedMaxAge = assumedMaxAge;
        this.assumedVirginRecruits = assumedVirginRecruits;
        this.assumedLengthBinCm = assumedLengthBinCm;
        this.assumedVarA = assumedVarA;
        this.assumedVarB = assumedVarB;
        this.assumedLenghtAtMaturity = assumedLenghtAtMaturity;
    }


    //this is just the yearly reporter
    @VisibleForTesting
    public double computeSPR() {


        double spr = SPR.computeSPR(
                new StructuredAbundance(abundance),
                species,
                assumedNaturalMortality,
                assumedKParameter,
                assumedLinf,
                assumedMaxAge,
                assumedVirginRecruits,
                assumedLengthBinCm,
                new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer age) {
                        return assumedVarA/1000 * Math.pow(species.getLengthAtAge(age, 0), assumedVarB);
                    }
                },
                new Function<Integer, Double>() {
                    @Override
                    public Double apply(Integer age) {
                        return species.getLengthAtAge(age, 0) < assumedLenghtAtMaturity ? 0d : 1d;
                    }

                }


        );


        return spr;

    }


    @VisibleForTesting
    public double computeMaturityRatio(){
        double matureCatch = 0;
        double allCatches = 0;
        for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++) {
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                allCatches += abundance[subdivision][bin];
                if(species.getLength(subdivision,bin)>= assumedLenghtAtMaturity)
                    matureCatch+=abundance[subdivision][bin];
            }
        }
        assert matureCatch <= allCatches;
        return matureCatch/allCatches;
    }

    private void reset(){

        //clear
        for(int subdivision=0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
            Arrays.fill(abundance[subdivision],0);
    }

    //steps each day
    @VisibleForTesting
    public void observeFishers()
    {

        for(Fisher fisher : sampledFishers)
        {
            for(int subdivision = 0; subdivision< abundance.length; subdivision++)
                for(int bin = 0; bin< abundance[0].length; bin++) {
                    double unitWeight = assumedVarA/1000 * Math.pow(species.getLength(subdivision,bin), assumedVarB);
                    abundance[subdivision][bin] += (fisher.getDailyCounter().getSpecificLandings(species, subdivision,
                                                                                                 bin)) /unitWeight;
                }
        }


    }




    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        for(Fisher fisher : model.getFishers())
        {
            if(samplingSelector.test(fisher)){
                fisher.getTags().add(surveyTag+" "+species);
                sampledFishers.add(fisher);
            }
        }

        abundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        //every day, collect information
        model.scheduleEveryDay(new Steppable() {
            @Override
            public void step(SimState simState) {
                observeFishers();
            }
        }, StepOrder.DAILY_DATA_GATHERING);

        model.scheduleEveryYear(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        reset();
                    }
                },
                StepOrder.DATA_RESET
        );

        model.getYearlyDataSet().registerGatherer("SPR " + species + " " + surveyTag,
                                                  new Gatherer<FishState>() {
                                                      @Override
                                                      public Double apply(FishState fishState) {
                                                          double spr = computeSPR();
                                                          return spr;

                                                      }
                                                  },Double.NaN);


        model.getYearlyDataSet().registerGatherer("Percentage Mature Catches " + species + " " + surveyTag,
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        double ratio = computeMaturityRatio();
                        return ratio;

                    }
                },Double.NaN);

        for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++) {
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                int finalSubdivision = subdivision;
                int finalBin = bin;
                String columnName = species + " " + "Catches(#) " + subdivision + "." + bin + " " + surveyTag;
                model.getYearlyDataSet().registerGatherer(
                        columnName,
                        new Gatherer<FishState>() {
                            @Override
                            public Double apply(FishState fishState) {

                                return abundance[finalSubdivision][finalBin];
                            }
                        }, Double.NaN
                );
            }

        }



    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        sampledFishers.clear();
    }
}
