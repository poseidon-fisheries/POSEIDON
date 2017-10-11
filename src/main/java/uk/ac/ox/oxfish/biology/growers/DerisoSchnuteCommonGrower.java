package uk.ac.ox.oxfish.biology.growers;

import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *  Grower using Deriso Schnute formula.
 *  It aggregates all given biologies into one, reproduce biomass that way and
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

    private double lastStepRecruits;



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
    private LinkedHashSet<BiomassLocalBiology> biologies = new LinkedHashSet<>();
    /**
     * receipt to stop the grower when needed
     */
    private Stoppable stoppable;


    /**
     * this is the OPTIONAL time series of survival rates used to initialize the model
     * Assume the "last" element of the list is the newest.
     * if not given, it will be filled with natural survival rates (assuming virgin state)
     */
    private final List<Double> empiricalSurvivalRates;



    public DerisoSchnuteCommonGrower(
            List<Double> empiricalYearlyBiomasses, double rho, double naturalSurvivalRate, double recruitmentSteepness,
            int recruitmentLag, int speciesIndex, double weightAtRecruitment, double weightAtRecruitmentMinus1,
            double initialRecruits) {
        this(empiricalYearlyBiomasses,null,rho,naturalSurvivalRate,recruitmentSteepness,
             recruitmentLag,speciesIndex,weightAtRecruitment,weightAtRecruitmentMinus1,initialRecruits);
    }


    public DerisoSchnuteCommonGrower(
            //doesn't ask for the virgin biomass because it discovers it on its own
            //by looking at carrying capacity
            List<Double> empiricalYearlyBiomasses,
            List<Double> empiricalSurvivalRates,
            double rho, double naturalSurvivalRate, double recruitmentSteepness,
            int recruitmentLag, int speciesIndex, double weightAtRecruitment, double weightAtRecruitmentMinus1,
            double initialRecruits) {
        this.empiricalSurvivalRates = empiricalSurvivalRates;
        this.empiricalYearlyBiomasses = empiricalYearlyBiomasses;
        this.rho = rho;
        this.naturalSurvivalRate = naturalSurvivalRate;
        this.recruitmentSteepness = recruitmentSteepness;
        this.recruitmentLag = recruitmentLag;
        this.speciesIndex = speciesIndex;
        this.weightAtRecruitment = weightAtRecruitment;
        this.weightAtRecruitmentMinus1 = weightAtRecruitmentMinus1;
        this.lastStepRecruits = initialRecruits;
    }

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


        //read from data or bootstrap it as just natural survival rate
        if(empiricalSurvivalRates != null)
        {
            for(int i=0; i<2; i++)
                actualSurvivalRates.addFirst(empiricalSurvivalRates.
                        get(empiricalSurvivalRates.size()-i-1));
        }
        else
        {
            actualSurvivalRates.add(naturalSurvivalRate);
            actualSurvivalRates.add(naturalSurvivalRate);
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
                lastStepRecruits
        );
        double newBiomass =  bioStep.getBiomass();
        lastStepRecruits = bioStep.getRecruits();

        //reallocate uniformly. Do not allocate above carrying capacity

        List<BiomassLocalBiology> biologyList = new ArrayList<>(this.biologies);

        double toReallocate = newBiomass  - currentBiomass; // I suppose this could be negative

        if( Math.abs(toReallocate) < FishStateUtilities.EPSILON ) //if there is nothing to allocate, ignore
            return;

        if(toReallocate > 0) //if we are adding biomass, keep only not-full biologies
            biologyList = biologyList.stream().filter(new Predicate<BiomassLocalBiology>() {
                @Override
                public boolean test(BiomassLocalBiology loco) {
                    return loco.getCurrentBiomass()[speciesIndex]< loco.getCarryingCapacity(speciesIndex);
                }
            }).collect(Collectors.toList());
        else {
            assert toReallocate < 0;
            //if we are removing biomass, keep only not-empty biologies
            biologyList = biologyList.stream().filter(new Predicate<BiomassLocalBiology>() {
                @Override
                public boolean test(BiomassLocalBiology loco) {
                    return loco.getCurrentBiomass()[speciesIndex] > 0;
                }
            }).collect(Collectors.toList());
        }


        //while there is still reallocation to be done
        MersenneTwisterFast random = ((FishState) simState).getRandom();

        while(Math.abs(toReallocate) > FishStateUtilities.EPSILON && !biologyList.isEmpty())
        {
            //pick a biology at random
            BiomassLocalBiology local = biologyList.get(random.nextInt(biologyList.size()));
            //give or take some biomass out
            double delta = toReallocate / (double) biologyList.size();
            local.getCurrentBiomass()[speciesIndex] += delta;
            //if you gave some biomass
            if(delta > 0)
            {
                //account for it
                toReallocate -= delta;
                //but if it's above carrying capacity, take it back
                double excess =  local.getCurrentBiomass()[speciesIndex] - local.getCarryingCapacity(speciesIndex);
                if(excess > FishStateUtilities.EPSILON) {
                    toReallocate += excess;
                    local.getCurrentBiomass()[speciesIndex] = local.getCarryingCapacity(speciesIndex);
                    biologyList.remove(local); //this biology is not going to accept any more
                }
            }
            //if you took biomass back
            else
            {
                //account for it
                toReallocate -= delta;
                //but if there is negative fish, put it back!
                if(local.getCurrentBiomass()[speciesIndex] < 0 ) {
                    toReallocate -= local.getCurrentBiomass()[speciesIndex];
                    local.getCurrentBiomass()[speciesIndex] = 0d;
                    biologyList.remove(local); //this biology is not going to accept any more
                }
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
