/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Pair;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * class that keeps track of what is caught per bin in a subset of fishers
 */
public class CatchSampler {


    /**
     * object that returns true whenever the fisher is to be sampled for
     */
    private final Predicate<Fisher> samplingSelector;


    /**
     * which species are we sampling
     */
    private final Species species;


    /**
     * here we keep the weight of fish that we are tracking!
     */
    private final double[][] landings;

    /**
     * the abundance (#) vector we are interested in; instantiated lazily
     */
    private double[][] abundanceToReturn;

    /**
     * here we keep all the fishers we sample when we are counting for abundance
     */
    private final List<Fisher> observedFishers = new LinkedList<>();


    /**
     * if this is not null, we are going to tag all the fishers we are surveying with this.
     */
    @Nullable
    private final String surveyTag;


    public CatchSampler(
            Predicate<Fisher> samplingSelector,
            Species species,
            @Nullable String surveyTag) {
        this.samplingSelector = samplingSelector;
        this.species = species;
        this.landings = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        abundanceToReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];
        this.surveyTag = surveyTag;
    }

    /**
     * clear the previous list of fishers
     */
    public void resetObservedFishers(){
        if(surveyTag!=null) {
            String totalTag = surveyTag+" "+species;
            for (Fisher fisher : observedFishers) {
                fisher.getTags().remove(totalTag);
            }
        }
        observedFishers.clear();
    }


    /**
     * builds the list of fishers to observe
     * @param model the model
     */
    public void checkWhichFisherToObserve(FishState model){

        resetObservedFishers();

        for(Fisher fisher : model.getFishers())
        {
            if(samplingSelector.test(fisher))
            {
                if(surveyTag!=null)
                    fisher.getTags().add(surveyTag+" "+species);
                observedFishers.add(fisher);
            }
        }


    }



    /**
     * supposedly what you'd step on: looks at all the fishers we are sampling and sum up all their landings and ADD it to the abundance vector.
     * Because fishers store their landings in weight, we need a function to turn them back into abundance. Here
     * we use the REAL weight function to do so
     */
    public void observe()
    {

        for(Fisher fisher : observedFishers)
        {
            for(int subdivision = 0; subdivision< landings.length; subdivision++)
                for(int bin = 0; bin< landings[0].length; bin++) {
                    // assumedVarA/1000 * Math.pow(species.getLength(subdivision,bin), assumedVarB);
                    landings[subdivision][bin] += (fisher.getDailyCounter().getSpecificLandings(species, subdivision,
                            bin));
                }
        }


    }


    /**
     * when we need to zero the abundance array, call this.
     */
    public void resetLandings(){

        //clear
        for(int subdivision=0; subdivision<species.getNumberOfSubdivisions(); subdivision++)
            Arrays.fill(landings[subdivision], 0);
    }


    /**
     * Getter for property 'abundance'.
     *
     * @return Value for property 'abundance'.
     */
    public double[][] getAbundance() {



        return getAbundance(new Function<Pair<Integer, Integer>, Double>() {
            @Override
            public Double apply(Pair<Integer, Integer> subdivisionBin) {
                return species.getWeight(subdivisionBin.getFirst(),subdivisionBin.getSecond());
            }
        });
    }


    public double[][] getAbundance(Function<Pair<Integer,Integer>,Double> subdivisionBinToWeightFunction){

        abundanceToReturn = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];


        for(int subdivision = 0; subdivision< landings.length; subdivision++)
            for(int bin = 0; bin< landings[0].length; bin++) {
                double unitWeight = subdivisionBinToWeightFunction.apply(new Pair<>(subdivision,bin));
                // assumedVarA/1000 * Math.pow(species.getLength(subdivision,bin), assumedVarB);
                abundanceToReturn[subdivision][bin] += landings[subdivision][bin] /unitWeight;
            }


        return abundanceToReturn;
    }

    /**
     * Getter for property 'samplingSelector'.
     *
     * @return Value for property 'samplingSelector'.
     */
    public Predicate<Fisher> getSamplingSelector() {
        return samplingSelector;
    }

    /**
     * Getter for property 'species'.
     *
     * @return Value for property 'species'.
     */
    public Species getSpecies() {
        return species;
    }

    /**
     * Getter for property 'surveyTag'.
     *
     * @return Value for property 'surveyTag'.
     */
    @Nullable
    public String getSurveyTag() {
        return surveyTag;
    }
}
