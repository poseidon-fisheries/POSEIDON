package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.LogisticLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Grower using Deriso Schnute formula. This one applies it separately to each biomass given
 * Created by carrknight on 1/31/17.
 */
public class DerisoSchnuteIndependentGrower implements Startable, Steppable{




    private List<LogisticLocalBiology> biologies = new LinkedList<>();


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
    private Map<LogisticLocalBiology,LinkedList<Double>> previousBiomasses =
            new HashMap<>();

    /**
     * map containing survival rates after fishing has occurred, last is the latest/newest
     */
    private Map<LogisticLocalBiology,LinkedList<Double>> actualSurvivalRates =
            new HashMap<>();

    /**
     * set up when starting
     */
    Stoppable stoppable;

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

        for(LogisticLocalBiology biology : biologies)
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
            double weightAtRecruitment, double weightAtRecruitmentMinus1) {
        this.empiricalYearlyBiomasses = empiricalYearlyBiomasses;
        this.rho = rho;
        this.naturalSurvivalRate = naturalSurvivalRate;
        this.recruitmentSteepness = recruitmentSteepness;
        this.recruitmentLag = recruitmentLag;
        this.speciesIndex = speciesIndex;
        this.weightAtRecruitment = weightAtRecruitment;
        this.weightAtRecruitmentMinus1 = weightAtRecruitmentMinus1;
    }

    /**
     * Appends all of the elements in the specified collection to the end of
     * this list, in the order that they are returned by the specified
     * collection's iterator (optional operation).  The behavior of this
     * operation is undefined if the specified collection is modified while
     * the operation is in progress.  (Note that this will occur if the
     * specified collection is this list, and it's nonempty.)
     *
     * @param c collection containing elements to be added to this list
     * @return <tt>true</tt> if this list changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *         is not supported by this list
     * @throws ClassCastException if the class of an element of the specified
     *         collection prevents it from being added to this list
     * @throws NullPointerException if the specified collection contains one
     *         or more null elements and this list does not permit null
     *         elements, or if the specified collection is null
     * @throws IllegalArgumentException if some property of an element of the
     *         specified collection prevents it from being added to this list
     * @see #add(Object)
     */
    public boolean addAll(Collection<? extends LogisticLocalBiology> c) {
        return biologies.addAll(c);
    }


    /**
     * Getter for property 'biologies'.
     *
     * @return Value for property 'biologies'.
     */
    public List<LogisticLocalBiology> getBiologies() {
        return biologies;
    }

    @Override
    public void step(SimState simState) {


        //remove all the biologies that stopped
        biologies = biologies.stream().filter(
                logisticLocalBiology -> !logisticLocalBiology.isStopped()).collect(Collectors.toList());

        //for each place
        for(LogisticLocalBiology biology : biologies)
        {

            //basic current info
            double currentBiomass = biology.getCurrentBiomass()[speciesIndex];
            double virginBiomass = biology.getCarryingCapacity(speciesIndex);
            LinkedList<Double> previousBiomasses = this.previousBiomasses.get(biology);
            LinkedList<Double> actualSurvivalRates = this.actualSurvivalRates.get(biology);


            double newBiomass = computeNewBiomassDerisoSchnute(currentBiomass, virginBiomass, previousBiomasses,
                                                               actualSurvivalRates,
                                                               naturalSurvivalRate, recruitmentLag,
                                                               recruitmentSteepness, weightAtRecruitment, rho,
                                                               weightAtRecruitmentMinus1);
            biology.getCurrentBiomass()[speciesIndex] = newBiomass;
        }


        if(biologies.size()==0) //if you removed all the biologies then we are done
            turnOff();

    }

    /**
     * computes new biomass and updates both the previous biomasses and actual survival rates lists
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
    public static double computeNewBiomassDerisoSchnute(
            double currentBiomass, double virginBiomass, LinkedList<Double> previousBiomasses,
            LinkedList<Double> actualSurvivalRates, double naturalSurvivalRate, int recruitmentLag,
            double recruitmentSteepness, double weightAtRecruitment,
            double rho, double weightAtRecruitmentMinus1) {
        Double previousBiomass = previousBiomasses.getLast();

        //compute the actual survival rate
        double trueSurvivalRate = currentBiomass/ previousBiomass;
        trueSurvivalRate *= naturalSurvivalRate;
        LinkedList<Double> survivalRates = actualSurvivalRates;
        survivalRates.remove(); //remove oldest
        survivalRates.add(trueSurvivalRate); //add new
        assert survivalRates.size()==2;

        //
        LinkedList<Double> queue = previousBiomasses;
        assert  queue.size() == recruitmentLag;
        double biomassAtRecruitment = queue.remove();
        queue.add(currentBiomass);
        assert  queue.size() == recruitmentLag;


        //spawner-recruit function

        double recruitment = (4*recruitmentSteepness*biomassAtRecruitment/virginBiomass);
        recruitment /= (1-recruitmentSteepness) +
                (5d*recruitmentSteepness-1d)*(biomassAtRecruitment/virginBiomass);

/*
            double temp1 = ((5d*recruitmentSteepness-1)/(4d*recruitmentSteepness));
            double temp2 = (1d-biomassAtRecruitment/virginBiomass);
            double recruitment = biomassAtRecruitment/(1d-temp1*temp2);

*/

        double survivedBiomass = naturalSurvivalRate * currentBiomass;
        double recruitmentBiomass =  weightAtRecruitment * recruitment;
        double somaticGrowth = rho * naturalSurvivalRate * currentBiomass -
                rho * survivalRates.get(0) * survivalRates.get(1) * previousBiomass -
                rho  * naturalSurvivalRate * weightAtRecruitmentMinus1  * recruitmentBiomass;

        return survivedBiomass + recruitmentBiomass + somaticGrowth;
    }
}
