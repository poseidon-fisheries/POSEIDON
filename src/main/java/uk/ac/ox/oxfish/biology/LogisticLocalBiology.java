package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.Arrays;

/**
 * A simple local biology that has carrying capacity and actual biomass. It then grows each species,
 * each year through logistic regression. There is no link/movement to other biologies.
 * Created by carrknight on 5/8/15.
 */
public class LogisticLocalBiology extends AbstractBiomassBasedBiology implements Startable {

    /**
     * the current amount of biomass in this spot
     */
    private Double[] currentBiomass;

    /**
     * the maximum amount of biomass
     */
    private Double[] carryingCapacity;


    /**
     * initialize the local biology
     * @param currentBiomass the biomass available
     * @param carryingCapacity the maximum amount of fish
     */
    public LogisticLocalBiology(
            Double[] currentBiomass, Double[] carryingCapacity) {
        Preconditions.checkArgument(currentBiomass.length==carryingCapacity.length);

        this.currentBiomass = Arrays.copyOf(currentBiomass,currentBiomass.length);
        this.carryingCapacity =Arrays.copyOf(carryingCapacity,carryingCapacity.length);

    }


    /**
     * create a logistic local biology where we specify how much of the biomass is currently available
     * @param carryingCapacity
     * @param species
     * @param random
     * @param initialMaxCapacity max proportion 0 to 1 of carrying capacity that might be available at this cell
     * @param initialMinCapacity min proportion 0 to 1 of carrying capacity that might be available at this cell
     */
    public LogisticLocalBiology(
            double carryingCapacity, int species,
            MersenneTwisterFast random, double initialMaxCapacity, double initialMinCapacity)
    {
        assert initialMaxCapacity>= initialMinCapacity;
        assert  initialMaxCapacity >=0;
        assert  initialMinCapacity <=1;
        this.carryingCapacity = new Double[species];
        Arrays.fill(this.carryingCapacity, carryingCapacity);
        this.currentBiomass = new Double[species];

        for(int i=0; i<currentBiomass.length; i++)
        {
            currentBiomass[i] = ((initialMaxCapacity - initialMinCapacity)*random.nextDouble(true, true) + initialMinCapacity)
                    * carryingCapacity;
        }
    }


    public LogisticLocalBiology(
            double carryingCapacity, int species,
            MersenneTwisterFast random)
    {
        this(carryingCapacity, species, random, 1, 0);
    }

    /**
     * the biomass at this location for a single species.
     *
     * @param species the species you care about
     * @return the biomass of this species
     */
    @Override
    public Double getBiomass(Species species) {
        final int index = species.getIndex();
        if(index>=this.currentBiomass.length)
            return 0d; //don't have it
        else
            return this.currentBiomass[index];
    }

    /**
     * the carrying capacity of this location for this species
     * @param species the species
     * @return the carrying capacity for this species at this location
     */
    public Double getCarryingCapacity(Species species)
    {
        final int index = species.getIndex();
        if(index>=this.carryingCapacity.length)
            return 0d; //don't have it
        else
            return this.carryingCapacity[index];
    }

    /**
     * the carrying capacity of this location for this species
     * @param index the species
     * @return the carrying capacity for this species at this location
     */
    public Double getCarryingCapacity(int index)
    {
        if(index>=this.carryingCapacity.length)
            return 0d; //don't have it
        else
            return this.carryingCapacity[index];
    }





    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *
     * @param species        the species fished
     * @param biomassFished the biomass fished
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(Species species, Double biomassFished) {

        final int specieIntex = species.getIndex();
        Preconditions.checkArgument(specieIntex < currentBiomass.length || biomassFished == 0,
                                    "you can't fish species that aren't here");


        if(specieIntex < currentBiomass.length)
        {
            currentBiomass[specieIntex]-= biomassFished;
            Preconditions.checkState(currentBiomass[specieIntex] >=0, "fished more biomass than available");
        }

    }





    /**
     *  set a new carrying capacity, might modify the current biomass
     * @param s the specie
     * @param newCarryingCapacity the new carrying capacity
     */
    public void setCarryingCapacity(Species s, double newCarryingCapacity)
    {
        Preconditions.checkArgument(newCarryingCapacity >= 0, "new carrying capacity must be positive");

        final int index = s.getIndex();
        if(index >=currentBiomass.length)
            growArrays(index +1);
        carryingCapacity[index] = newCarryingCapacity;
        //don't let currentbiomass be above carryingCapacity
        currentBiomass[index] = Math.min(currentBiomass[index],newCarryingCapacity);
        assert currentBiomass[index]>=0;
    }


    /**
     * sets the new current biomass. Must be lower than carrying capacity
     * @param s the specie
     * @param newCurrentBiomass the new biomass in lbs
     */
    public void setCurrentBiomass(Species s, double newCurrentBiomass)
    {

        Preconditions.checkArgument(newCurrentBiomass >= 0, "new biomass can't be negative!");
        final int index = s.getIndex();
        if(index >=currentBiomass.length)
            growArrays(index +1);
        Preconditions.checkArgument(currentBiomass[index] <= carryingCapacity[index],
                                    "the new current biomass is higher than carrying capacity!");

        currentBiomass[index] = newCurrentBiomass;
    }





    /**
     * proof that you have started
     */
    private boolean started;

    private boolean stopped;


    /**
     * schedule to act each year
     * @param model the model to schedule on
     */
    @Override
    public void start(FishState model) {

        Preconditions.checkArgument(!started,"Already started");

        started=true;
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {



        stopped = true;

    }

    /**
     * Getter for property 'stopped'.
     *
     * @return Value for property 'stopped'.
     */
    public boolean isStopped() {
        return stopped;
    }

    /**
     * pad with zeros
     * @param newSize new array size
     */
    private void growArrays(int newSize)
    {
        final int oldSize = currentBiomass.length;
        assert oldSize < newSize;
        currentBiomass = Arrays.copyOf(currentBiomass,newSize);
        carryingCapacity = Arrays.copyOf(carryingCapacity,newSize);
        //fill them
        for(int i=oldSize; i<newSize; i++)
        {
            currentBiomass[i]=0d;
            carryingCapacity[i]=0d;
        }
    }


    public Double[] getCurrentBiomass() {
        return currentBiomass;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LogisticLocalBiology{");
        sb.append("currentBiomass=").append(Arrays.toString(currentBiomass));
        sb.append('}');
        return sb.toString();
    }
}
