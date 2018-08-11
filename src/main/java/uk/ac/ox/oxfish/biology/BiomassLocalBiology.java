/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

import java.util.Arrays;

/**
 * A simple local biology that has carrying capacity and actual biomass.
 * Created by carrknight on 5/8/15.
 */
public class BiomassLocalBiology extends AbstractBiomassBasedBiology implements Startable,
        VariableBiomassBasedBiology {

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
    public BiomassLocalBiology(
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
    public BiomassLocalBiology(
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


    public BiomassLocalBiology(
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
    @Override
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
    @Override
    public Double getCarryingCapacity(int index)
    {
        if(index>=this.carryingCapacity.length)
            return 0d; //don't have it
        else
            return this.carryingCapacity[index];
    }





    /**
     * Tells the local biology that a fisher (or something anyway) fished this much biomass from this location
     *  @param caught
     * @param notDiscarded
     * @param biology
     */
    @Override
    public void reactToThisAmountOfBiomassBeingFished(
            Catch caught, Catch notDiscarded, GlobalBiology biology) {

        Preconditions.checkArgument(!caught.hasAbundanceInformation(),
                                    "using abundance driven catches with biomass driven biology!");

        for(int speciesIndex =0; speciesIndex< caught.numberOfSpecies(); speciesIndex++)
        {
            //if you caught anything
            double biomassFishedOut = caught.getWeightCaught(speciesIndex);
            if(biomassFishedOut > 0 && !biology.getSpecie(speciesIndex).isImaginary())
            {
                assert currentBiomass[speciesIndex]>=biomassFishedOut :
                                            "going to fish more biomass than available for species " + speciesIndex + " , fishedOut: " + biomassFishedOut +
                                                    ", currentBiomass " + currentBiomass[speciesIndex];
                currentBiomass[speciesIndex]-= biomassFishedOut;
                assert currentBiomass[speciesIndex] >=0 :
                                         "fished more biomass than available for species " + speciesIndex + " , fishedOut: " + biomassFishedOut +
                                                 ", currentBiomass " + currentBiomass[speciesIndex];
            }
        }




    }





    /**
     *  set a new carrying capacity, might modify the current biomass
     * @param s the specie
     * @param newCarryingCapacity the new carrying capacity
     */
    @Override
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
    @Override
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


    @Override
    public Double[] getCurrentBiomass() {
        return currentBiomass;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BiomassLocalBiology{");
        sb.append("currentBiomass=").append(Arrays.toString(currentBiomass));
        sb.append('}');
        return sb.toString();
    }
}
