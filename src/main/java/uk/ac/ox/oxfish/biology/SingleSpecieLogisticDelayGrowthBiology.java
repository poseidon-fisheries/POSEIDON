package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import com.google.common.collect.EvictingQueue;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

/**
 * This is not the full Deriso-Schnute model, rather it's just the initial equation given here:
 *  https://catalyst.uw.edu/workspace/file/download/d75d78b22acb18071f8ddc321cda7ed08a74f39da15311374e3c10e448045bc7
 *
 * Created by carrknight on 10/19/15.
 */
public class SingleSpecieLogisticDelayGrowthBiology implements LocalBiology, Steppable, Startable
{

    /**
     * how many years before current spawns enter the biomass proper
     */
    private final int yearDelays;

    /**
     * storage of all the past biomasses (for recruitment purposes)
     */
    private final EvictingQueue<Double> pastBiomass;

    /**
     * numerator parameter in the recruitment equation
     */
    final private double aParameter;

    /**
     * denominator parameter in the recruitment equation
     */
    final private double bParameter;


    /**
     * current biomass
     */
    private double currentBiomass;

    /**
     * biomass over which you can never go
     */
    final private double  maxBiomass;

    /**
     * the species modeled
     */
    private final Species species;
    private Stoppable stoppable;


    public SingleSpecieLogisticDelayGrowthBiology(
            Species species, double currentBiomass, double maxBiomass, int yearDelays, double aParameter,
            double bParameter) {
        Preconditions.checkArgument(yearDelays > 0, "Use undelayed biology rather than feeding 0 to a delayed one");
        Preconditions.checkArgument(maxBiomass > 0);
        Preconditions.checkArgument(currentBiomass <= maxBiomass);
        Preconditions.checkArgument(currentBiomass >= 0);
        this.species = species;
        this.yearDelays = yearDelays;
        pastBiomass = EvictingQueue.create(yearDelays);
        while(pastBiomass.remainingCapacity()>0)
            pastBiomass.add(currentBiomass);
        this.aParameter = aParameter;
        this.bParameter = bParameter;
        this.currentBiomass = currentBiomass;
        this.maxBiomass = maxBiomass;
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species)
    {
        if(species == this.species)
            return currentBiomass;
        else
            return null;
    }

    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param species        the species fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {
        if(species.equals(this.species))
        {
            Preconditions.checkArgument(biomassFished <= currentBiomass);
            currentBiomass-= biomassFished;
        }
    }

    @Override
    public void step(SimState simState)
    {

        double biologyAtRecruitment = pastBiomass.poll();
        assert pastBiomass.remainingCapacity() == 1;
        double recruitment = aParameter * biologyAtRecruitment / (bParameter + biologyAtRecruitment);

        currentBiomass = Math.min(currentBiomass + recruitment,maxBiomass);
        pastBiomass.add(currentBiomass);


    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        stoppable = model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        stoppable.stop();
    }

    public double getaParameter() {
        return aParameter;
    }

    public double getbParameter() {
        return bParameter;
    }

    public int getYearDelays() {
        return yearDelays;
    }
}
