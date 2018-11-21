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

package uk.ac.ox.oxfish.fisher.equipment;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * The inventory object of the ship. It has a maximum capacity. Any fish caught after reaching capacity is thrown overboard
 * Created by carrknight on 4/21/15.
 */
public class Hold {


    /**
     * weight of fish currently transported
     */
    private double weightCarried = 0;

    /**
     * maximum pounds that can be held
     */
    private double maximumLoad = 0;

    private double[] fishHold;

    /**
     * here we store fish per abundance; notice that we are storing its weight, not its raw numbers
     */
    private double[][] weightCaughtBinned;

    private final GlobalBiology biology;

    /**
     * create a new empty fishHold
     * @param maximumLoadInKg maximum capacity
     * @param biology
     */
    public Hold(double maximumLoadInKg, GlobalBiology biology)
    {
        this.maximumLoad = maximumLoadInKg;
        this.biology = biology;
        fishHold = new double[biology.getSize()];
    }

    /**
     * store the catch
     * @param caught the catch
     */
    public void load(Catch caught)
    {

        //use the appropriate method given that catch might be in biomass or abundance
        if(caught.hasAbundanceInformation()) {
            loadAbundanceCatch(caught);
            assert consistencyCheckAbundance();
        }
        else {
            loadBiomassCatch(caught);
            assert consistencyCheckBiomass();
        }

        assert weightCarried >=0;
        if(weightCarried > maximumLoad + FishStateUtilities.EPSILON/2)
            throwOverboard();
        assert  maximumLoad >= weightCarried || Math.abs(maximumLoad- weightCarried) <= FishStateUtilities.EPSILON;
    }

    private void loadAbundanceCatch(Catch caught) {


        //if this is the first time catching abundance, prepare the array!
        if(weightCaughtBinned == null)
        {
            Preconditions.checkArgument(getTotalWeightOfCatchInHold() == 0,
                                        " You are mixing abundance and non-abundance catches in hold; that's impossible");
            weightCaughtBinned = new double[caught.getBiomassArray().length][];
            for(int species = 0; species< weightCaughtBinned.length; species++)
                weightCaughtBinned[species] = new double[caught.getAbundance(species).getBins()];
        }

        //now fill it with fish
        for(int species = 0; species< weightCaughtBinned.length; species++) {
            StructuredAbundance abundance = caught.getAbundance(species);
            Meristics meristics = biology.getSpecie(species).getMeristics();

            for (int bin = 0; bin < abundance.getBins(); bin++)
            {
                double additionalWeight = FishStateUtilities.weigh(abundance,
                                                                   meristics, bin);
                weightCaughtBinned[species][bin] += additionalWeight;
                weightCarried += additionalWeight;
                fishHold[species] += additionalWeight;
            }
        }




    }

    private void loadBiomassCatch(Catch caught) {
        //start loading up
        for(int i=0; i< fishHold.length; i++)
        {
            double poundsCaught = caught.getWeightCaught(i);
            fishHold[i] += poundsCaught;
            weightCarried += poundsCaught;
            assert  poundsCaught >=0;
        }
    }

    /**
     * call this if you have loaded more than what you can carry. throws overboard catch proportionally
     */
    private void throwOverboard() {


        //this already modifies fishHold! so we just need to move abundance information to match it

        double proportionRemaining = throwOverboard(fishHold, maximumLoad);
        //throw away abundance as well!
        if(proportionRemaining<1d && hasAbundanceInformation())
        {
            for(int species = 0; species< weightCaughtBinned.length; species++) {
                for (int bin = 0; bin < weightCaughtBinned[species].length; bin++) {
                    weightCaughtBinned[species][bin] *= proportionRemaining;
                }
            }
        }


        weightCarried = Arrays.stream(fishHold).sum();
        assert !(hasAbundanceInformation()) ||
                consistencyCheckAbundance(); //check for consistency if needed
        assert Math.abs(weightCarried -maximumLoad)<=FishStateUtilities.EPSILON;
        assert Math.abs(getPercentageFilled() -1.0)<=FishStateUtilities.EPSILON;

    }

    /**
     * Proportionally reduces all catches until their sum is equal/below maximum load!
     * CAREFUL: modifies the argument!
     * @param fishHold
     * @param maximumLoad
     */
    public static double throwOverboard(double[] fishHold,double maximumLoad)
    {
        double currentLoad = 0;
        for(double held : fishHold)
            currentLoad+=held;
        if(currentLoad>maximumLoad)
        {
            double proportionToKeep = maximumLoad / (currentLoad);
            assert proportionToKeep <= 1 && proportionToKeep >= 0 : proportionToKeep;
            for (int i = 0; i < fishHold.length; i++) {
                fishHold[i] *= proportionToKeep;
            }
            return proportionToKeep;
        }
        return 1d;
    }

    public double getTotalWeightOfCatchInHold() {
        return weightCarried;
    }

    public double getWeightOfCatchInHold(Species species) {
        return fishHold[species.getIndex()];
    }


    public double getMaximumLoad() {
        return maximumLoad;
    }

    public double getPercentageFilled()
    {
        return weightCarried /maximumLoad;
    }

    private boolean consistencyCheckBiomass()
    {
        double sum = 0;
        for(double pounds : fishHold)
            sum+=pounds;
        return  Math.abs(weightCarried -sum) < FishStateUtilities.EPSILON;
    }

    private boolean consistencyCheckAbundance(){
        double sum = 0;
        for(int i=0; i<biology.getSize(); i++)
        {
            double speciesSum=0;
            for(int bin = 0; bin< weightCaughtBinned[i].length; bin++)
            {
                sum+= weightCaughtBinned[i][bin];
                speciesSum+= weightCaughtBinned[i][bin];
            }
            //the fish holds ought to be updated correctly!
            if(Math.abs(fishHold[i] -speciesSum) > FishStateUtilities.EPSILON)
                return false;
        }
        //the kgs ought to be updated correctly
        return  Math.abs(weightCarried -sum) < FishStateUtilities.EPSILON;


    }



    public Catch  unload()
    {
        Catch toReturn = new Catch(fishHold);
        weightCaughtBinned = null;
        fishHold = new double[fishHold.length];
        weightCarried = 0;
        assert consistencyCheckBiomass();
        return toReturn;
    }


    public Hold makeCopy()
    {
        Hold toReturn = new Hold(maximumLoad,this.biology);
        toReturn.fishHold = Arrays.copyOf(fishHold,fishHold.length);
        toReturn.weightCarried = this.weightCarried;
        toReturn.maximumLoad = this.maximumLoad;
        if(weightCaughtBinned !=null) {
            toReturn.weightCaughtBinned = new double[weightCaughtBinned.length][];
            for (int i = 0; i < weightCaughtBinned.length; i++)
                toReturn.weightCaughtBinned[i] = Arrays.copyOf(weightCaughtBinned[i], weightCaughtBinned[i].length);
        }
        return toReturn;
    }

    public boolean hasAbundanceInformation()
    {
        return weightCaughtBinned != null;
    }


    public double getWeightOfBin(Species species, int bin)
    {
        Preconditions.checkArgument(hasAbundanceInformation());
        return weightCaughtBinned[species.getIndex()][bin];
    }

}
