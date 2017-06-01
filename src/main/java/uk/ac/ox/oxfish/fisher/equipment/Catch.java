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

        //weigh them (assuming they are all men!)
        biomassCaught = abundanceToBiomass(biology);
        totalWeight = computeTotalWeight();


    }

    private double[] abudanceToBiomassVector(GlobalBiology biology) {
        return new double[biology.getSize()];
    }


    /**
     * create a catch object given the abundance of each species binned per age/length
     * @param ageStructure binned abundance per species
     * @param biology the biology object containing info about each species
     */
    public Catch(int[][] ageStructure, GlobalBiology biology )
    {
        Preconditions.checkArgument(biology.getSize() == ageStructure.length);
        this.abundance = new StructuredAbundance[ageStructure.length];
        for(int i=0; i<ageStructure.length; i++)
            abundance[i] = new StructuredAbundance(ageStructure[i]);

        //weigh them (assuming they are all men!)
        biomassCaught = abundanceToBiomass(biology);
        totalWeight = computeTotalWeight();


    }

    /**
     *
     * @param maleAbundance male abundance per species per bin
     * @param femaleAbundance female abundance per species per bin
     * @param biology biology
     */
    public Catch(int[][] maleAbundance, int[][]femaleAbundance, GlobalBiology biology )
    {
        Preconditions.checkArgument(biology.getSize() == maleAbundance.length);
        this.abundance = new StructuredAbundance[maleAbundance.length];
        for(int i=0; i<maleAbundance.length; i++)
            abundance[i] = new StructuredAbundance(maleAbundance[i],femaleAbundance[i]);

        //weigh them (assuming they are all men!)
        biomassCaught = abundanceToBiomass(biology);

        totalWeight = computeTotalWeight();

    }

    private double[] abundanceToBiomass(GlobalBiology biology) {
        double[] biomasses = new double[biology.getSize()];
        for(Species species : biology.getSpecies())
            biomasses[species.getIndex()] =
                    FishStateUtilities.weigh(
                            abundance[species.getIndex()],
                            species);
        return biomasses;
    }

    /**
     * single species abundance catch
     * @param maleAbundance
     * @param femaleAbundance
     * @param correctSpecies
     * @param biology
     */
    public Catch(int[] maleAbundance, int[]femaleAbundance, Species correctSpecies, GlobalBiology biology )
    {
        this.abundance = new StructuredAbundance[maleAbundance.length];
        for(Species index : biology.getSpecies())
        {
            if(correctSpecies==index)
                abundance[index.getIndex()] = new StructuredAbundance(maleAbundance,femaleAbundance);
            else
                abundance[index.getIndex()] = new StructuredAbundance(new int[index.getMaxAge()+1],
                                                                      new int[index.getMaxAge()+1]);
        }
        //weigh them (assuming they are all men!)
        biomassCaught = new double[biology.getSize()];
        for(Species species : biology.getSpecies())
            biomassCaught[species.getIndex()] =
                    FishStateUtilities.weigh(
                            abundance[species.getIndex()],
                            species);
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
        return Arrays.stream(biomassCaught).sum();
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
}
