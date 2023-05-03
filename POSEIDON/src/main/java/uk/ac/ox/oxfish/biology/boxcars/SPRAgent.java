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
import org.jetbrains.annotations.NotNull;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries.EFFORT;

public class SPRAgent implements AdditionalStartable, Steppable {


    private final String surveyTag;

    private final Species species;

    private final double assumedLinf;


    private final double assumedKParameter;

    private double assumedNaturalMortality;

    private final int assumedMaxAge;

    private final double assumedVirginRecruits;

    private final double assumedLengthBinCm;

    private final double assumedVarA;

    private final double assumedVarB;

    private final double assumedLenghtAtMaturity;

    private final Function<Pair<Integer, Integer>, Double> binLengthToWeightFunction;

    /**
     * object sampling fishers to keep track of their landings
     */
    final protected CatchAtLengthSampler sampler;

    final private SPRFormula formula;


    public SPRAgent(
            String surveyTag, Species species,
            Predicate<Fisher> samplingSelector,
            double assumedLinf,
            double assumedKParameter, double assumedNaturalMortality,
            int assumedMaxAge,
            double assumedVirginRecruits,
            double assumedLengthBinCm, double assumedVarA, double assumedVarB, double assumedLenghtAtMaturity,
            SPRFormula formula) {

        this(
                surveyTag,
                species,
                new StochasticCatchSampler(samplingSelector,species,surveyTag),
                assumedLinf,
                assumedKParameter, assumedNaturalMortality,
                assumedMaxAge,
                assumedVirginRecruits,
                assumedLengthBinCm, assumedVarA, assumedVarB, assumedLenghtAtMaturity,
                formula
        );



    }

    public SPRAgent(
            String surveyTag, Species species,
            CatchAtLengthSampler sampler,
            double assumedLinf,
            double assumedKParameter, double assumedNaturalMortality,
            int assumedMaxAge,
            double assumedVirginRecruits,
            double assumedLengthBinCm, double assumedVarA, double assumedVarB, double assumedLenghtAtMaturity,
            SPRFormula formula) {
        /**
         * object that returns true whenever the fisher is to be sampled for this SPR computation
         */
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
        binLengthToWeightFunction = new Function<Pair<Integer, Integer>, Double>() {
            @Override
            public Double apply(Pair<Integer, Integer> subBinPair) {

                return assumedVarA / 1000 * Math.pow(species.getLength(subBinPair.getFirst(),
                        subBinPair.getSecond()), assumedVarB);
            }
        };
        this.sampler = sampler;
        this.formula = formula;

    }


    //this is just the yearly reporter
    @VisibleForTesting
    public double computeSPR() {


        return formula.computeSPR(this,new StructuredAbundance(sampler.getAbundance(
                binLengthToWeightFunction
        )));

    }

    @NotNull
    public Function<Integer, Double> getAgeToMaturityFunction() {
        return new Function<Integer, Double>() {
            @Override
            public Double apply(Integer age) {
                return species.getLengthAtAge(age, 0) < assumedLenghtAtMaturity ? 0d : 1d;
            }

        };
    }

    @NotNull
    public Function<Integer, Double> getAgeToWeightFunction() {
        return new Function<Integer, Double>() {
            @Override
            public Double apply(Integer age) {
                return assumedVarA / 1000 * Math.pow(species.getLengthAtAge(age, 0), assumedVarB);
            }
        };
    }


    /**
     * computes % of ABUNDANCE (raw number) of the catch above Lmat
     * @return
     */
    @VisibleForTesting
    public double computeMaturityRatio(){
        double matureCatch = 0;
        double allCatches = 0;
        double[][] abundance = sampler.getAbundance(
                binLengthToWeightFunction
        );
        for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++) {
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                assert Double.isFinite(abundance[subdivision][bin]) || bin==0; //for some formulas weight at 0 length is undefined
                if(Double.isFinite(abundance[subdivision][bin])) {
                    allCatches += abundance[subdivision][bin];
                    if (species.getLength(subdivision, bin) >= assumedLenghtAtMaturity)
                        matureCatch += abundance[subdivision][bin];
                }
            }
        }
        assert matureCatch <= allCatches;
        return matureCatch/allCatches;
    }

    public double computeMeanLength(){
        double numberOfObservations = 0;
        double sumLength = 0;
        double[][] abundance = sampler.getAbundance(
                binLengthToWeightFunction
        );
        for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++) {
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                assert Double.isFinite(abundance[subdivision][bin]) || bin==0; //for some formulas weight at 0 length is undefined
                if(Double.isFinite(abundance[subdivision][bin])) {
                    numberOfObservations += abundance[subdivision][bin];
                    sumLength += abundance[subdivision][bin] * species.getLength(subdivision, bin);
                }
            }
        }

        return sumLength/numberOfObservations;
    }

    /**
     * computes % of ABUNDANCE (raw number) of the catch above lopt
     * @return
     */
    @VisibleForTesting
    public double computeLoptRatio(){
        /**
         * the guessed length at which you'd catch it if you had perfect info would be...
         *
         * formula is embedded in the sql computations but you can find it, for example, in https://doi.org/10.1093/icesjms/fsy078
         * rounded to keep it consistent
         */
        int lopt = (int) Math.round(assumedLinf * (3/(3+assumedNaturalMortality/assumedKParameter)));



        double superMatureCatch = 0;
        double allCatches = 0;
        //weight abundance for each bin
        double[][] abundance = sampler.getAbundance(
                binLengthToWeightFunction
        );
        for(int subdivision =0; subdivision<species.getNumberOfSubdivisions(); subdivision++) {
            for (int bin = 0; bin < species.getNumberOfBins(); bin++) {
                assert Double.isFinite(abundance[subdivision][bin]) || bin == 0; //for some formulas weight at 0 length is undefined
                if (Double.isFinite(abundance[subdivision][bin])) {
                    allCatches += abundance[subdivision][bin];
                    if (species.getLength(subdivision, bin) >= lopt)
                        superMatureCatch += abundance[subdivision][bin];
                }
            }
        }
        assert superMatureCatch <= allCatches;
        return superMatureCatch/allCatches;
    }



    /**
     * this is the daily step: delegate the observation to the catch sampler. This agent self-schedules so there is
     * no need to call this directly
     * @param simState
     */
    @VisibleForTesting
    @Override
    public void step(SimState simState) {
        sampler.observeDaily(


        );
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        //Preconditions.checkArgument(sampler==null, "SPR Agent already Started!!");
        sampler.start(model);

        //every day, collect information
        model.scheduleEveryDay(this, StepOrder.DAILY_DATA_GATHERING);

        model.scheduleEveryYear(
                new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        sampler.resetCatchObservations();
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


        model.getYearlyDataSet().registerGatherer("CPUE " + species + " " + surveyTag,
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        double numerator = 0;
                        final double[][] observedLandings = sampler.getLandings();
                        for (double[] landingsPerSubdivision : observedLandings) {
                            for (double landingsPerBin : landingsPerSubdivision) {
                                numerator+=landingsPerBin;
                            }
                        }
                        double denominator=0;
                        for (Fisher fisher : sampler.viewObservedFishers())
                            denominator+=fisher.getYearlyCounterColumn(EFFORT);
                        return numerator/denominator;
                    }
                },Double.NaN);



        model.getYearlyDataSet().registerGatherer("Landings " + species + " " + surveyTag,
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        double landings = 0;
                        final double[][] observedLandings = sampler.getLandings();
                        for (double[] landingsPerSubdivision : observedLandings) {
                            for (double landingsPerBin : landingsPerSubdivision) {
                                landings+=landingsPerBin;
                            }
                        }
                        return landings;
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

        model.getYearlyDataSet().registerGatherer("Percentage Lopt Catches " + species + " " + surveyTag,
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        double ratio = computeLoptRatio();
                        return ratio;

                    }
                },Double.NaN);

        model.getYearlyDataSet().registerGatherer("Mean Length Caught " + species + " " + surveyTag,
                new Gatherer<FishState>() {
                    @Override
                    public Double apply(FishState fishState) {
                        return computeMeanLength();

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

                                return sampler.getAbundance(
                                        new Function<Pair<Integer, Integer>, Double>() {
                                            @Override
                                            public Double apply(Pair<Integer, Integer> subdivisionBinPair) {
                                                return  assumedVarA/1000 * Math.pow(
                                                        species.getLength(subdivisionBinPair.getFirst(),
                                                                subdivisionBinPair.getSecond()),
                                                        assumedVarB);

                                            }
                                        }

                                )[finalSubdivision][finalBin];
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

        sampler.turnOff();
    }


    public List<Fisher> monitorObservedFishers() {
        return sampler.viewObservedFishers();
    }


    public Species getSpecies() {
        return species;
    }

    public double getAssumedLinf() {
        return assumedLinf;
    }

    public double getAssumedKParameter() {
        return assumedKParameter;
    }

    public double getAssumedNaturalMortality() {
        return assumedNaturalMortality;
    }

    public int getAssumedMaxAge() {
        return assumedMaxAge;
    }

    public double getAssumedVirginRecruits() {
        return assumedVirginRecruits;
    }

    public double getAssumedLengthBinCm() {
        return assumedLengthBinCm;
    }

    public double getAssumedVarA() {
        return assumedVarA;
    }

    public double getAssumedVarB() {
        return assumedVarB;
    }

    public double getAssumedLenghtAtMaturity() {
        return assumedLenghtAtMaturity;
    }

    public Function<Pair<Integer, Integer>, Double> getBinLengthToWeightFunction() {
        return binLengthToWeightFunction;
    }


    public void setAssumedNaturalMortality(double assumedNaturalMortality) {
        this.assumedNaturalMortality = assumedNaturalMortality;
    }

    public String getSurveyTag() {
        return surveyTag;
    }
}
