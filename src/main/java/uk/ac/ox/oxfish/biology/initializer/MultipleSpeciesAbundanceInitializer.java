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

package uk.ac.ox.oxfish.biology.initializer;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.InitialAbundanceFromFileFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;

/**
 * Create multiple species, each abundance (count) based rather than biomass based
 * Created by carrknight on 3/17/16.
 */
public class MultipleSpeciesAbundanceInitializer implements AllocatedBiologyInitializer
{

    public static final String FAKE_SPECIES_NAME = "Others";


    private final boolean rounding;
    /**
     * the path to the biology folder, which must contain a count.csv and a meristic.yaml file
     */
    private final LinkedHashMap<String,Path> biologicalDirectories;


    /**
     * scales down the number of fish present by multiplying it by scaling
     */
    private final double scaling;

    /**
     * when true the recruits get redistributed according to the original geographical distribution rather than the
     * current one
     */
    private final boolean fixedRecruitmentDistribution;
    private final boolean preserveLastAge;

    /**
     * boolean representing whether or not we should add "others" as a mock species to account in the model
     * for everything else that is not directly modeled?
     */
    private final boolean addOtherSpecies;
    private String countFileName = "count.csv";

    public MultipleSpeciesAbundanceInitializer(
            LinkedHashMap<String, Path> biologicalDirectories, double scaling,
            boolean fixedRecruitmentDistribution, final boolean mortality100Percent, boolean addOtherSpecies,
            boolean rounding) {
        this.biologicalDirectories = biologicalDirectories;
        this.scaling = scaling;
        this.fixedRecruitmentDistribution = fixedRecruitmentDistribution;
        this.preserveLastAge = !mortality100Percent;
        this.addOtherSpecies = addOtherSpecies;
        this.rounding = rounding;
    }

    /**
     * defines the proportion of fish going to any sea-tile. No checks are made that the
     * proportions sum up to one so be careful!
     */
    private final HashMap<Species,
            Function<SeaTile, Double>> allocators = new LinkedHashMap<>();



    /**
     * list of all the abundance based local biologies
     */
    private final LinkedHashMap<SeaTile,AbundanceLocalBiology> locals = new LinkedHashMap<>();


    /**
     * contains all the mortality+recruitment processes of each species
     */
    private final LinkedHashMap<Species,SingleSpeciesNaturalProcesses> naturalProcesses = new LinkedHashMap<>();


    /**
     * holds the "total count" of fish as initially read from data
     */
    private final LinkedHashMap<Species, double[][]> initialAbundance = new LinkedHashMap<>();

    /**
     * the generate local made static so the MultipleSpeciesInitializer can use it too
     *
     * @param biology global biology file
     * @param seaTile seatile
     * @param locals a map seatiles---> abundance local biologies that gets filled if this is not a land tile
     * @return empty biology on land, abundance biology in water
     */
    public static LocalBiology generateAbundanceBiologyExceptOnLand(
            GlobalBiology biology, SeaTile seaTile, HashMap<SeaTile, AbundanceLocalBiology> locals) {
        if (seaTile.isLand())
            return new EmptyLocalBiology();




        AbundanceLocalBiology local = new AbundanceLocalBiology(biology);
        locals.put(seaTile,local);
        return local;
    }

    /**
     * read up a folder that contains meristics.yaml and turn it into a species object
     * @param biologicalDirectory the folder containing meristics.yaml
     * @param speciesName the name of the species
     * @return the new species
     * @throws IOException
     */
    public static Species generateSpeciesFromFolder(Path biologicalDirectory, String speciesName) throws IOException {
        FishYAML yaml = new FishYAML();
        String meristicFile = String.join("\n", Files.readAllLines(biologicalDirectory.resolve("meristics.yaml")));
        MeristicsInput input = yaml.loadAs(meristicFile, MeristicsInput.class);
        return new Species(speciesName, input);
    }




    public static SingleSpeciesNaturalProcesses initializeNaturalProcesses(

            FishState model, Species species,
            Map<SeaTile, AbundanceLocalBiology> locals,
            StockAssessmentCaliforniaMeristics meristics,
            boolean preserveLastAge,
            int yearDelay,
            boolean rounding
    ){

        AgingProcess agingProcess = new StandardAgingProcess(preserveLastAge);
        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(
                yearDelay > 0 ?
                        new RecruitmentBySpawningBiomassDelayed(
                                meristics.getVirginRecruits(),
                                meristics.getSteepness(),
                                meristics.getCumulativePhi(),
                                meristics.isAddRelativeFecundityToSpawningBiomass(),
                                meristics.getMaturity(),
                                meristics.getRelativeFecundity(), FishStateUtilities.FEMALE,
                                yearDelay) :
                        new RecruitmentBySpawningBiomass(
                                meristics.getVirginRecruits(),
                                meristics.getSteepness(),
                                meristics.getCumulativePhi(),
                                meristics.isAddRelativeFecundityToSpawningBiomass(),
                                meristics.getMaturity(),
                                meristics.getRelativeFecundity(), FishStateUtilities.FEMALE, false),
                species,
                rounding, agingProcess,
                new NoAbundanceDiffusion(),
                new ExponentialMortalityProcess(meristics),
                false);
        for (Map.Entry<SeaTile, AbundanceLocalBiology> entry : locals.entrySet()) {
            processes.add(entry.getValue(),entry.getKey());
        }
        model.registerStartable(processes);
        return processes;

    }

    public double[][] getInitialAbundance (Species species)
    {
        return initialAbundance.get(species);
    }


    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *  @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     * @param map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {
        return generateAbundanceBiologyExceptOnLand(biology,seaTile,
                                                                                      locals);
    }


    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(MersenneTwisterFast random, FishState modelBeingInitialized) {

        List<Species> speciesList = new LinkedList<>();

        try {
            for(Map.Entry<String,Path> directory : biologicalDirectories.entrySet())
            {
                speciesList.add(
                        generateSpeciesFromFolder(directory.getValue(),
                                                  directory.getKey()));


            }
            //need to add an additional species to catch "all"
            if(addOtherSpecies)
                speciesList.add(new Species(FAKE_SPECIES_NAME, StockAssessmentCaliforniaMeristics.FAKE_MERISTICS, true));


            return new GlobalBiology(speciesList.toArray(new Species[speciesList.size()]));
        }catch (IOException e) {
            e.printStackTrace();
            Log.error("Failed to instantiate the species because I couldn't find the meristics.yaml file in the folder provided");

        }
        System.exit(-1);
        return null;

    }


    /**
     * after all the tiles have been instantiated this method gets called once to put anything together or to smooth
     * biomasses or whatever
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {

        try {
            for (Species species : biology.getSpecies())
            {

                if(addOtherSpecies && biologicalDirectories.get(species.getName()) == null)
                {
                    Preconditions.checkState(species.getName().equals(FAKE_SPECIES_NAME),
                                             "Do not have biological directory for species " + species.getName());
                    naturalProcesses.put(species,new MockNaturalProcess(species));
                    continue;
                }

                InitialAbundanceFromFileFactory factory =
                        new InitialAbundanceFromFileFactory(
                                biologicalDirectories.get(species.getName()).resolve(countFileName)
                        );
                double[][] totalCount = factory.apply(model).getInitialAbundance();
                initialAbundance.put(species,totalCount);

                //prepare the map biology-->ratio of fish to put there
                LinkedHashMap<AbundanceLocalBiology, Double> currentWeightMap = new LinkedHashMap<>(locals.size());
                initialWeights.put(species, currentWeightMap);
                //we start with location--->ratio of fish so we need to go location--->biology through the allocator
                turnLocationRatioMaptoBiologyRatioMap(species, currentWeightMap);

                //we should have covered all locations by now
                assert locals.values().containsAll(currentWeightMap.keySet());
                assert currentWeightMap.keySet().containsAll(locals.values());

                //now that we have the ratio and the count for each biology object assign the correct number of fish to each!
                resetAllLocalBiologies(species, totalCount, currentWeightMap);

                //start the natural process (use single species abundance since it's easier)
                SingleSpeciesNaturalProcesses process = initializeNaturalProcesses(
                        model,species,locals, ((StockAssessmentCaliforniaMeristics) species.getMeristics()),
                        preserveLastAge,
                        0,
                        rounding);
                //if you want to keep recruits to spawn in the same places this is the time to do it
                if(fixedRecruitmentDistribution) {
                    process.setRecruitsAllocator(
                            new BiomassAllocator() {
                                @Override
                                public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {
                                    return currentWeightMap.get(locals.get(tile));
                                }
                            }
                    );
                }
                naturalProcesses.put(species,process);
            }

            //now go back and set as wastelands all tiles that have 0 fish
            tileloop:
            for(SeaTile tile : map.getAllSeaTilesExcludingLandAsList())
            {
                for(Species species : biology.getSpecies())
                    if(tile.getBiomass(species)>0)
                        continue tileloop;
                //if you are here, the place is barren; let's just switch to a empty local biomass
                tile.setBiology(new EmptyLocalBiology());
                assert !tile.isFishingEvenPossibleHere();
            }

            //done!
            biologicalDirectories.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.error("Failed to locate or read count.csv correctly. Could not instantiate the local biology");
            System.exit(-1);
        }


    }

    private void turnLocationRatioMaptoBiologyRatioMap(
            Species species, HashMap<AbundanceLocalBiology, Double> currentWeightMap) {
        Function<SeaTile, Double> allocator = allocators.get(species);
        Preconditions.checkArgument(allocator != null);
        //fill in the location
        for(Map.Entry<SeaTile,AbundanceLocalBiology> local : locals.entrySet())
        {

            //find the ratio by allocator
            double ratio = allocator.apply(local.getKey());
            currentWeightMap.put(local.getValue(),ratio);
        }
    }


    /**
     * you must at all time be ready to reset local biology to its pristine state
     *
     * @param species species you want the biomass resetted
     */
    @Override
    public void resetLocalBiology(Species species) {

        resetAllLocalBiologies(species,
                               initialAbundance.get(species),
                               initialWeights.get(species));

    }

    public void resetAllLocalBiologies(
            Species speciesToReset, double[][] newTotalFishCount,
            HashMap<AbundanceLocalBiology, Double> biologyToProportionOfFishThere)
    {
        if(speciesToReset.getName().equals(FAKE_SPECIES_NAME))
            return;

        LinkedHashMap<AbundanceLocalBiology,Double> ratiosLocalCopy = new LinkedHashMap<>(biologyToProportionOfFishThere);
        for (Map.Entry<AbundanceLocalBiology, Double> ratio : biologyToProportionOfFishThere.entrySet()) {
            //if this is not present in the local list
            if(!locals.values().contains(ratio.getKey()))
            {
                //it must be that we assumed this was a wasteland and will still be so!
                Preconditions.checkArgument(ratio.getValue()==0);
                //remove it from the local copy!
                ratiosLocalCopy.remove(ratio.getKey());
            }

        }

        Preconditions.checkArgument(locals.values().containsAll(ratiosLocalCopy.keySet()),
                                    "Some local biologies in the proportion map are not present in the initializer list");
        Preconditions.checkArgument(ratiosLocalCopy.keySet().containsAll(locals.values()),
                                    "Some local biologies in the masterlist are not present in the proportion map");

        double maleRemainder[] = new double[speciesToReset.getNumberOfBins()];
        double femaleRemainder[] = new double[speciesToReset.getNumberOfBins()];
        for(Map.Entry<AbundanceLocalBiology,Double> ratio : ratiosLocalCopy.entrySet())
        {

            //get the ratio back
            AbundanceLocalBiology local = ratio.getKey();
            StructuredAbundance abundance = local.getAbundance(speciesToReset);
            Preconditions.checkArgument(abundance.getSubdivisions()==2, "coded for ");
            for(int i=0; i<speciesToReset.getNumberOfBins(); i++)
            {


                double doubleMale = scaling * newTotalFishCount[FishStateUtilities.MALE][i] * ratio.getValue()  +
                        maleRemainder[i];

                abundance.asMatrix()[FishStateUtilities.MALE][i] = doubleMale;
                double doubleFemale = scaling * newTotalFishCount[FishStateUtilities.FEMALE][i] * ratio.getValue()  +
                        femaleRemainder[i];
                abundance.asMatrix()[FishStateUtilities.FEMALE][i] = doubleFemale;


                if(rounding)
                {
                    abundance.asMatrix()[FishStateUtilities.MALE][i] = (int) abundance.asMatrix()[FishStateUtilities.MALE][i];
                    maleRemainder[i] =  (doubleMale-abundance.asMatrix()[FishStateUtilities.MALE][i]);
                    abundance.asMatrix()[FishStateUtilities.FEMALE][i] = (int) abundance.asMatrix()[FishStateUtilities.FEMALE][i];
                    femaleRemainder[i] =  (doubleFemale-abundance.asMatrix()[FishStateUtilities.FEMALE][i]);

                }



            }
        }
        if(Log.DEBUG)
            Log.debug(speciesToReset + " resetted to total biomass: " +
                         locals.values().stream().mapToDouble(value -> value.getBiomass(speciesToReset)).sum());
    }

    /**
     * holds the weight given to each biology object when first created
     */
    private final LinkedHashMap<Species,HashMap<AbundanceLocalBiology,Double>> initialWeights = new LinkedHashMap<>();





    /**
     * puts the function describing the % of biomass that will initially be allocated to this sea-tile
     */
    public Function<SeaTile, Double> putAllocator(
            Species key,
            Function< SeaTile, Double> value) {
        return allocators.put(key, value);
    }

    public int getNumberOfFishableTiles(){
        return locals.size();
    }

    /**

     */
    public HashMap<AbundanceLocalBiology,Double> getInitialWeights(Species species) {
        return initialWeights.get(species);
    }

    /**
     * Getter for property 'locals'.
     *
     * @return Value for property 'locals'.
     */
    public HashMap<SeaTile, AbundanceLocalBiology> getLocals() {
        return locals;
    }

    /**
     * Getter for property 'naturalProcesses'.
     *
     * @return Value for property 'naturalProcesses'.
     */
    public SingleSpeciesNaturalProcesses getNaturalProcesses(Species species) {
        return naturalProcesses.get(species);
    }

    /**
     * Getter for property 'countFileName'.
     *
     * @return Value for property 'countFileName'.
     */
    public String getCountFileName() {
        return countFileName;
    }

    /**
     * Setter for property 'countFileName'.
     *
     * @param countFileName Value to set for property 'countFileName'.
     */
    public void setCountFileName(String countFileName) {
        this.countFileName = countFileName;
    }
}
