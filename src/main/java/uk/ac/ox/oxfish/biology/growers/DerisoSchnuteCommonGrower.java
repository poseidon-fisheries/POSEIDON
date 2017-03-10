package uk.ac.ox.oxfish.biology.growers;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

/**
 *  Grower using Deriso Schnute formula. It aggregates all given biologies into one, reproduce biomass that way and
 *  redistribute it uniformly.
 * Created by carrknight on 2/2/17.
 */
public class DerisoSchnuteCommonGrower implements Startable, Steppable {


    private final List<Double> empiricalYearlyBiomasses;

    private final double rho;

    private final double naturalSurvivalRate;

    private final double recruitmentSteepness;

    private final int recruitmentLag;

    private final int speciesIndex;

    private final double weightAtRecruitment;

    private final double weightAtRecruitmentMinus1;

    private double previousRecruits;


    public DerisoSchnuteCommonGrower(
            List<Double> empiricalYearlyBiomasses, double rho, double naturalSurvivalRate, double recruitmentSteepness,
            int recruitmentLag, int speciesIndex, double weightAtRecruitment, double weightAtRecruitmentMinus1,
            double initialRecruits) {
        this.empiricalYearlyBiomasses = empiricalYearlyBiomasses;
        this.rho = rho;
        this.naturalSurvivalRate = naturalSurvivalRate;
        this.recruitmentSteepness = recruitmentSteepness;
        this.recruitmentLag = recruitmentLag;
        this.speciesIndex = speciesIndex;
        this.weightAtRecruitment = weightAtRecruitment;
        this.weightAtRecruitmentMinus1 = weightAtRecruitmentMinus1;
        this.previousRecruits = initialRecruits;
    }

    /**
     * queue containing previous end of the year biomasses, last is the latest/newest
     */
    private LinkedList<Double> previousBiomasses =
            new LinkedList<>();

    /**
     * queue containing survival rates after fishing has occurred, last is the latest/newest
     */
    private LinkedList<Double> actualSurvivalRates =  new LinkedList<>();

    /**
     * list of local biologies we manage. We will at all times
     */
    private Set<BiomassLocalBiology> biologies = new HashSet<>();
    /**
     * receipt to stop the grower when needed
     */
    private Stoppable stoppable;

    /**
     * very much like the independent grower, except we do not divide the biomasses by number of cells
     *
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {



        //populates biomasses from data
        for(int i=0; i<recruitmentLag; i++)
            previousBiomasses.addFirst(empiricalYearlyBiomasses.
                    get(empiricalYearlyBiomasses.size()-i-1));
        assert previousBiomasses.size() == recruitmentLag;

        //todo add fishing rate here
        actualSurvivalRates.add(naturalSurvivalRate);
        actualSurvivalRates.add(naturalSurvivalRate);


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
    @Override
    public void step(SimState simState) {



        //basic current info
        double currentBiomass = 0;
        double virginBiomass = 0;
        for(BiomassLocalBiology biology : biologies)
        {
            currentBiomass+= biology.getCurrentBiomass()[speciesIndex];
            virginBiomass+= biology.getCarryingCapacity(speciesIndex);

        }
        DerisoSchnuteIndependentGrower.DerisoSchnuteStep bioStep = DerisoSchnuteIndependentGrower.computeNewBiomassDerisoSchnute(
                currentBiomass,
                virginBiomass,
                previousBiomasses,
                actualSurvivalRates,
                naturalSurvivalRate,
                recruitmentLag,
                recruitmentSteepness,
                weightAtRecruitment,
                rho,
                weightAtRecruitmentMinus1,
                previousRecruits
        );
        double newBiomass =  bioStep.getBiomass();
        previousRecruits = bioStep.getRecruits();
        //reallocate uniformly. Do not allocate above carrying capacity
        List<BiomassLocalBiology> biologies = new ArrayList<>(this.biologies);
        Collections.shuffle(biologies);
        double toReallocate = newBiomass  - currentBiomass; // I suppose this could be negative

        for(int i=0; i<biologies.size(); i++)
        {

            BiomassLocalBiology local = biologies.get(i);
            local.getCurrentBiomass()[speciesIndex] += toReallocate/ (double)biologies.size();
            double excess =  local.getCarryingCapacity(speciesIndex) - local.getCurrentBiomass()[speciesIndex];
            //if there is too much
            if(excess< -FishStateUtilities.EPSILON) {
                assert local.getCarryingCapacity(speciesIndex) < local.getCurrentBiomass()[speciesIndex];
                local.getCurrentBiomass()[speciesIndex] = local.getCarryingCapacity(speciesIndex);
                //put this fish somewhere else!
                assert  - excess > 0;
                toReallocate += -excess;
            }

        }


    }

    public boolean addAll(Collection<? extends BiomassLocalBiology> c) {
        return biologies.addAll(c);
    }


    /**
     * Getter for property 'empiricalYearlyBiomasses'.
     *
     * @return Value for property 'empiricalYearlyBiomasses'.
     */
    public List<Double> getEmpiricalYearlyBiomasses() {
        return empiricalYearlyBiomasses;
    }

    /**
     * Getter for property 'rho'.
     *
     * @return Value for property 'rho'.
     */
    public double getRho() {
        return rho;
    }

    /**
     * Getter for property 'naturalSurvivalRate'.
     *
     * @return Value for property 'naturalSurvivalRate'.
     */
    public double getNaturalSurvivalRate() {
        return naturalSurvivalRate;
    }

    /**
     * Getter for property 'recruitmentSteepness'.
     *
     * @return Value for property 'recruitmentSteepness'.
     */
    public double getRecruitmentSteepness() {
        return recruitmentSteepness;
    }

    /**
     * Getter for property 'recruitmentLag'.
     *
     * @return Value for property 'recruitmentLag'.
     */
    public int getRecruitmentLag() {
        return recruitmentLag;
    }

    /**
     * Getter for property 'speciesIndex'.
     *
     * @return Value for property 'speciesIndex'.
     */
    public int getSpeciesIndex() {
        return speciesIndex;
    }

    /**
     * Getter for property 'weightAtRecruitment'.
     *
     * @return Value for property 'weightAtRecruitment'.
     */
    public double getWeightAtRecruitment() {
        return weightAtRecruitment;
    }

    /**
     * Getter for property 'weightAtRecruitmentMinus1'.
     *
     * @return Value for property 'weightAtRecruitmentMinus1'.
     */
    public double getWeightAtRecruitmentMinus1() {
        return weightAtRecruitmentMinus1;
    }

    /**
     * Getter for property 'previousBiomasses'.
     *
     * @return Value for property 'previousBiomasses'.
     */
    public LinkedList<Double> getPreviousBiomasses() {
        return previousBiomasses;
    }

    /**
     * Getter for property 'actualSurvivalRates'.
     *
     * @return Value for property 'actualSurvivalRates'.
     */
    public LinkedList<Double> getActualSurvivalRates() {
        return actualSurvivalRates;
    }

    /**
     * Getter for property 'biologies'.
     *
     * @return Value for property 'biologies'.
     */
    public Set<BiomassLocalBiology> getBiologies() {
        return biologies;
    }
}
