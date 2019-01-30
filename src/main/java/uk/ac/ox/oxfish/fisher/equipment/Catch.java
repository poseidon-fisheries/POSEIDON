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
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.Arrays;

/**
 * Right now this is just a map specie--->pounds caught. It might in the future deal with age and other factors which is
 * why I create the object catch rather than just using a map
 * Created by carrknight on 4/20/15.
 */
public class Catch {


    private final double[] biomassCaught;

    /**
     * optionally this object can contain the age structure of the catch
     * first index is species <br>
     */
    @Nullable
    private final StructuredAbundance[] abundance;


    private final double totalWeight;


    /**
     * single species catch
     * @param species the species caught
     * @param poundsCaught the pounds that have been caugh
     */
    public Catch(Species species, double poundsCaught, GlobalBiology biology) {
       this(species,poundsCaught,biology.getSize());


    }

    /**
     * single species catch
     * @param species the species caught
     * @param poundsCaught the pounds that have been caugh
     */
    public Catch(Species species, double poundsCaught, int numberOfSpeciesInTheModel) {
        Preconditions.checkState(poundsCaught >=0);
        biomassCaught = new double[numberOfSpeciesInTheModel];
        biomassCaught[species.getIndex()] = poundsCaught;
        abundance = null;

        totalWeight = poundsCaught;


    }

    private double computeTotalWeight()
    {
        double weight = 0;
        for(double biomass : biomassCaught)
        {
            Preconditions.checkArgument(biomass>=0, "can't fish negative weight!");
            weight += biomass;
        }
        return weight;
    }

    public Catch(double[] catches)
    {

        this.biomassCaught = catches;
        abundance =null;
        totalWeight = computeTotalWeight();

    }



    /**
     * create a catch object given the abundance of each species binned per age/length
     * @param abundance binned abundance per species
     * @param biology the biology object containing info about each species
     */
    public Catch(StructuredAbundance[] abundance, GlobalBiology biology )
    {
        this.abundance = abundance;
        Preconditions.checkArgument(biology.getSize() == abundance.length);

        //weigh them
        biomassCaught = abundanceToBiomass(biology);
        totalWeight = computeTotalWeight();


    }

    /**
     * create a catch object given the abundance of each species binned per age/length
     * @param ageStructure binned abundance per species
     * @param biology the biology object containing info about each species
     */
    public Catch(double[][] ageStructure, GlobalBiology biology )
    {
        Preconditions.checkArgument(biology.getSize() == ageStructure.length);
        this.abundance = new StructuredAbundance[ageStructure.length];
        for(int i=0; i<ageStructure.length; i++)
            abundance[i] = new StructuredAbundance(ageStructure[i]);

        //weigh them
        biomassCaught = abundanceToBiomass(biology);
        totalWeight = computeTotalWeight();


    }

    /**
     *
     * @param maleAbundance male abundance per species per bin
     * @param femaleAbundance female abundance per species per bin
     * @param biology biology
     */
    public Catch(double[][] maleAbundance, double[][]femaleAbundance, GlobalBiology biology )
    {
        Preconditions.checkArgument(biology.getSize() == maleAbundance.length);
        this.abundance = new StructuredAbundance[maleAbundance.length];
        for(int i=0; i<maleAbundance.length; i++)
            abundance[i] = new StructuredAbundance(maleAbundance[i],femaleAbundance[i]);

        //weigh them
        biomassCaught = abundanceToBiomass(biology);

        totalWeight = computeTotalWeight();

    }

    private double[] abundanceToBiomass(GlobalBiology biology) {
        double[] biomasses = new double[biology.getSize()];
        for(Species species : biology.getSpecies())
            biomasses[species.getIndex()] =
                    FishStateUtilities.weigh(
                            abundance[species.getIndex()],
                            species.getMeristics());
        return biomasses;
    }

    /**
     * single species abundance catch
     * @param maleAbundance
     * @param femaleAbundance
     * @param correctSpecies
     * @param biology
     */
    public Catch(double[] maleAbundance, double[]femaleAbundance, Species correctSpecies, GlobalBiology biology )
    {
        this.abundance = new StructuredAbundance[biology.getSize()];
        for(Species index : biology.getSpecies())
        {
            if(correctSpecies==index)
                abundance[index.getIndex()] = new StructuredAbundance(maleAbundance,femaleAbundance);
            else
                abundance[index.getIndex()] = new StructuredAbundance(new double[index.getNumberOfBins()],
                                                                      new double[index.getNumberOfBins()]);
        }
        //weigh them (assuming they are all men!)
        biomassCaught = new double[biology.getSize()];
        for(Species species : biology.getSpecies())
            biomassCaught[species.getIndex()] =
                    FishStateUtilities.weigh(
                            abundance[species.getIndex()],
                            species.getMeristics());
        totalWeight = computeTotalWeight();

    }


    public double getWeightCaught(Species species)
    {
        return biomassCaught[species.getIndex()];
    }

    public double getWeightCaught(int index)
    {
        return biomassCaught[index];
    }

    public int numberOfSpecies(){
        return biomassCaught.length;
    }

    public double getWeightCaught(Species species, int bin)
    {
        Preconditions.checkArgument(hasAbundanceInformation());
        return FishStateUtilities.weigh(abundance[species.getIndex()],species.getMeristics(),bin);
    }


    public double getWeightCaught(Species species,int subdivision, int bin)
    {
        Preconditions.checkArgument(hasAbundanceInformation());
        return FishStateUtilities.weigh(abundance[species.getIndex()],species.getMeristics(),subdivision,bin);
    }

    @Nullable
    public StructuredAbundance getAbundance(Species species)
    {
        return getAbundance(species.getIndex());
    }

    @Nullable
    public StructuredAbundance getAbundance(int index)
    {
        if(abundance == null)
            return null;
        return abundance[index];
    }



    public double totalCatchWeight()
    {
        double sum =0;
        for (double caught : biomassCaught) {
            sum+=caught;

        }
        return sum;
    }

    @Override
    public String toString() {
        return Arrays.toString(biomassCaught);

    }

    /**
     * Getter for property 'totalWeight'.
     *
     * @return Value for property 'totalWeight'.
     */
    public double getTotalWeight() {
        return totalWeight;
    }

    public boolean hasAbundanceInformation(){
        return abundance !=null;
    }

    /**
     * returns a copy of the biomass copies
     */
    public double[] getBiomassArray() {
        return Arrays.copyOf(biomassCaught,biomassCaught.length);
    }


    /**
     * returns a new Catch object which represents the sum of two separate catch objects.
     * It assumes that both inputs are congruent (they either both have abundance information or they both don't).
     * THIS IS NOT SAFE AND WILL RUIN FIRST CATCH numbers; save ahead!
     * @param first the first catch to sum (WILL BE MODIFIED AS SIDE EFFECT)
     * @param second
     * @return
     */
    public static Catch sumCatches(Catch first,
                                   Catch second)
    {
        if(first.hasAbundanceInformation())
        {
            Preconditions.checkState(second.hasAbundanceInformation(), "cannot sum up incongruent catches!");
            for(int species=0; species<first.abundance.length; species++)
            {
                double[][] matrixAbundance = first.abundance[species].asMatrix();
                double[][] toAdd = second.abundance[species].asMatrix();
                for(int i=0; i<matrixAbundance.length; i++)
                    for(int j=0; j<toAdd.length; j++)
                        matrixAbundance[i][j] += toAdd[i][j];

            }
            return first;

        }
        else{
            Preconditions.checkState(!second.hasAbundanceInformation(), "cannot sum up incongruent catches!");

            double[] biomass = new double[first.biomassCaught.length];
            for(int i=0; i<biomass.length; i++)
                biomass[i]= first.biomassCaught[i] + second.biomassCaught[i];
            return new Catch(biomass);


        }


    }
}
