package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Grower using Deriso Schnute formula. This one applies it separately to each biomass given
 * Created by carrknight on 1/31/17.
 */
public class DerisoSchnuteIndependentGrower implements Startable, Steppable{




    private List<BiomassLocalBiology> biologies = new LinkedList<>();


    /**
     * this is the time series of biomass we use to initialize the model.
     * Assume the "last" element of the list is the newest.
     */
    private final List<Double> empiricalYearlyBiomasses;

    private final double rho;

    private final double naturalSurvivalRate;

    private final double recruitmentSteepness;

    private final int recruitmentLag;

    private final int speciesIndex;

    private final double weightAtRecruitment;

    private final double weightAtRecruitmentMinus1;

    /**
     * map containing previous end of the year biomasses, last is the latest/newest
     */
    private Map<BiomassLocalBiology,LinkedList<Double>> previousBiomasses =
            new HashMap<>();

    /**
     * map containing survival rates after fishing has occurred, last is the latest/newest
     */
    private Map<BiomassLocalBiology,LinkedList<Double>> actualSurvivalRates =
            new HashMap<>();


    private Map<BiomassLocalBiology,Double> previousRecruits = new HashMap<>();

    /**
     * set up when starting
     */
    private Stoppable stoppable;


    private double initialRecruits;

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        Preconditions.checkArgument(stoppable == null, "already started!");

        double numberOfCells = biologies.size();

        for(BiomassLocalBiology biology : biologies)
        {
            //populates biomasses from data
            LinkedList<Double> biomasses = new LinkedList<>();
            for(int i=0; i<recruitmentLag; i++)
                biomasses.addFirst(empiricalYearlyBiomasses.
                        get(empiricalYearlyBiomasses.size()-i-1) / numberOfCells);
            assert biomasses.size() == recruitmentLag;
            LinkedList<Double>  survivalRates = new LinkedList<>();

            //todo add fishing rate here
            survivalRates.add(naturalSurvivalRate);
            survivalRates.add(naturalSurvivalRate);

            previousBiomasses.put(biology,biomasses);
            actualSurvivalRates.put(biology,survivalRates);
            previousRecruits.put(biology,initialRecruits);
        }

        stoppable = model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if(stoppable!=null)
            stoppable.stop();
    }

    public DerisoSchnuteIndependentGrower(
            List<Double> empiricalYearlyBiomasses, double rho, double naturalSurvivalRate, double recruitmentSteepness,
            int recruitmentLag, int speciesIndex,
            double weightAtRecruitment, double weightAtRecruitmentMinus1,
            double virginBiomass, double initialRecruits) {
        this.empiricalYearlyBiomasses = empiricalYearlyBiomasses;
        this.rho = rho;
        this.naturalSurvivalRate = naturalSurvivalRate;
        this.recruitmentSteepness = recruitmentSteepness;
        this.recruitmentLag = recruitmentLag;
        this.speciesIndex = speciesIndex;
        this.weightAtRecruitment = weightAtRecruitment;
        this.weightAtRecruitmentMinus1 = weightAtRecruitmentMinus1;
        this.initialRecruits = initialRecruits;


        double virginRecruits =
                virginBiomass * (1d-(1+rho)*naturalSurvivalRate + rho * naturalSurvivalRate * naturalSurvivalRate)
                        /
                        (weightAtRecruitment - rho * naturalSurvivalRate * weightAtRecruitmentMinus1);

        double alpha = (1d-recruitmentSteepness)/(4d*recruitmentSteepness*virginRecruits);
        double beta = (5*recruitmentSteepness-1d)/(4d*recruitmentSteepness*virginRecruits);
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation).  The behavior of this
     * operation is undefined if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     */
    public boolean addAll(Collection<? extends BiomassLocalBiology> c) {
        return biologies.addAll(c);
    }


    /**
     * Getter for property 'biologies'.
     *
     * @return Value for property 'biologies'.
     */
    public List<BiomassLocalBiology> getBiologies() {
        return biologies;
    }

    @Override
    public void step(SimState simState) {


        //remove all the biologies that stopped
        biologies = biologies.stream().filter(
                logisticLocalBiology -> !logisticLocalBiology.isStopped()).collect(Collectors.toList());

        //for each place
        for(BiomassLocalBiology biology : biologies)
        {

            //basic current info
            double currentBiomass = biology.getCurrentBiomass()[speciesIndex];
            double virginBiomass = biology.getCarryingCapacity(speciesIndex);
            LinkedList<Double> previousBiomasses = this.previousBiomasses.get(biology);
            LinkedList<Double> actualSurvivalRates = this.actualSurvivalRates.get(biology);


            DerisoSchnuteStep derisoSchnuteStep = computeNewBiomassDerisoSchnute(currentBiomass, virginBiomass,
                                                                                 previousBiomasses,
                                                                                 actualSurvivalRates,
                                                                                 naturalSurvivalRate, recruitmentLag,
                                                                                 recruitmentSteepness,
                                                                                 weightAtRecruitment, rho,
                                                                                 weightAtRecruitmentMinus1,
                                                                                 previousRecruits.get(biology));
            biology.getCurrentBiomass()[speciesIndex] = derisoSchnuteStep.getBiomass();
            previousRecruits.put(biology,derisoSchnuteStep.getRecruits());
        }


        if(biologies.size()==0) //if you removed all the biologies then we are done
            turnOff();

    }

    /**
     * computes new biomass and recruits and updates all the given lists .previousBiomasses, actualSurvivalRates.
     * @param currentBiomass the biomass now (including fishing mortality but where the natural mortality has not been applied yet
     * @param virginBiomass the biomass expected with no fishing
     * @param previousBiomasses queue of previous biomasses, will get updated within this method. Last element in the queue is the newest
     * @param actualSurvivalRates queue size 2 of actual survival rates in the past. Last element in the queue is the newest
     * @param naturalSurvivalRate the survival rate of fish excluding fishing
     * @param recruitmentLag years between spawning and becoming a fish
     * @param recruitmentSteepness biomass to recruitment parameter
     * @param weightAtRecruitment weight at recruitment age
     * @param rho biological growth factor
     * @param weightAtRecruitmentMinus1 weight at year before recruitment
     * @return weight of new biomass (it's the caller responsibility to actually insert this number in the model)
     */
    public static DerisoSchnuteStep computeNewBiomassDerisoSchnute(
            double currentBiomass, double virginBiomass, LinkedList<Double> previousBiomasses,
            LinkedList<Double> actualSurvivalRates,
            double naturalSurvivalRate,
            int recruitmentLag,
            double recruitmentSteepness,
            double weightAtRecruitment,
            double rho,
            double weightAtRecruitmentMinus1,
            double previousNumberOfRecruits) {
        double virginRecruits =
                virginBiomass * (1d-(1+rho)*naturalSurvivalRate + rho * naturalSurvivalRate * naturalSurvivalRate)
                        /
                        (weightAtRecruitment - rho * naturalSurvivalRate * weightAtRecruitmentMinus1);

        double alpha = (1d-recruitmentSteepness)/(4d*recruitmentSteepness*virginRecruits);
        double beta = (5*recruitmentSteepness-1d)/(4d*recruitmentSteepness*virginRecruits);
        return computeNewBiomassDerisoSchnute(currentBiomass,virginBiomass,previousBiomasses,actualSurvivalRates,
                                              naturalSurvivalRate,recruitmentLag,weightAtRecruitment,rho,weightAtRecruitmentMinus1,
                                              previousNumberOfRecruits,alpha,beta);
    }

    /**
     *
     * @param currentBiomass the biomass now (including fishing mortality but where the natural mortality has not been applied yet
     * @param virginBiomass the biomass expected with no fishing
     * @param previousBiomasses queue of previous biomasses, will get updated within this method. Last element in the queue is the newest
     * @param actualSurvivalRates queue size 2 of actual survival rates in the past. Last element in the queue is the newest
     * @param naturalSurvivalRate the survival rate of fish excluding fishing
     * @param recruitmentLag years between spawning and becoming a fish
     * @param weightAtRecruitment weight at recruitment age
     * @param rho biological growth factor
     * @param weightAtRecruitmentMinus1 weight at year before recruitment
     * @param alpha Beverton's growth parameter
     * @param beta Beverton's growth parameter
     * @return weight of new biomass (it's the caller responsibility to actually insert this number in the model)
     */
    public static DerisoSchnuteStep computeNewBiomassDerisoSchnute(
            double currentBiomass, double virginBiomass,
            LinkedList<Double> previousBiomasses,
            LinkedList<Double> actualSurvivalRates,
            double naturalSurvivalRate,
            int recruitmentLag,
            double weightAtRecruitment,
            double rho,
            double weightAtRecruitmentMinus1,
            double previousNumberOfRecruits,
            double alpha,
            double beta)
    {

        Double previousBiomass = previousBiomasses.get(previousBiomasses.size()-2); //last is "current*fishingMortality" which isn't right for this equation

        //compute the new actual survival rate
        double trueSurvivalRate = currentBiomass/  previousBiomasses.getLast();
        trueSurvivalRate *= naturalSurvivalRate;
        LinkedList<Double> survivalRates = actualSurvivalRates;
        survivalRates.remove(); //remove oldest
        survivalRates.add(trueSurvivalRate); //add new
        assert survivalRates.size()==2; //you only need to keep track of the first two

        //store current biomass in the queue to simulate yearly delays in recruitment
        //basically spawners is biomass x years ago
        LinkedList<Double> queue = previousBiomasses;
        assert  queue.size() == recruitmentLag;
        double spawners = queue.remove();



        //new number of recruits
        double recruits = (spawners / virginBiomass) / (alpha + beta * spawners/virginBiomass);


       double biomass = (1+rho)*currentBiomass*naturalSurvivalRate - //we already kept track of the fishing mortality (the model did, anyway)
               rho * survivalRates.get(0) * survivalRates.get(1) * previousBiomass -
               rho * survivalRates.get(1) * weightAtRecruitmentMinus1 * previousNumberOfRecruits +
               weightAtRecruitment * recruits;

        queue.add(biomass);
        assert  queue.size() == recruitmentLag;

       return new DerisoSchnuteStep(FishStateUtilities.round(biomass), recruits);


    }


    //just a fancy pair
    public static class DerisoSchnuteStep{
        private final double biomass;
        private final double recruits;

        public DerisoSchnuteStep(double biomass, double recruits) {
            this.biomass = biomass;
            this.recruits = recruits;
        }

        /**
         * Getter for property 'recruits'.
         *
         * @return Value for property 'recruits'.
         */
        public double getRecruits() {
            return recruits;
        }

        /**
         * Getter for property 'biomass'.
         *
         * @return Value for property 'biomass'.
         */
        public double getBiomass() {
            return biomass;
        }
    }
}
