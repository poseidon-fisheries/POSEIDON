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
            boolean fixedRecruitmentDistribution, final boolean preserveLastAge, boolean addOtherSpecies) {
        this.biologicalDirectories = biologicalDirectories;
        this.scaling = scaling;
        this.fixedRecruitmentDistribution = fixedRecruitmentDistribution;
        this.preserveLastAge = preserveLastAge;
        this.addOtherSpecies = addOtherSpecies;
    }

    /**
     * defines the proportion of fish going to any sea-tile. No checks are made that the
     * proportions sum up to one so be careful!
     */
    private final HashMap<Species,
            Function<SeaTile, Double>> allocators = new HashMap<>();



    /**
     * list of all the abundance based local biologies
     */
    private final HashMap<SeaTile,AbundanceBasedLocalBiology> locals = new HashMap<>();


    /**
     * contains all the mortality+recruitment processes of each species
     */
    private final HashMap<Species,SingleSpeciesNaturalProcesses> naturalProcesses = new HashMap<>();


    /**
     * holds the "total count" of fish as initially read from data
     */
    private final HashMap<Species, int[][]> initialAbundance = new HashMap<>();

    /**
     * the generate local made static so the MultipleSpeciesInitializer can use it too
     *
     * @param biology global biology file
     * @param seaTile seatile
     * @param locals a map seatiles---> abundance local biologies that gets filled if this is not a land tile
     * @return empty biology on land, abundance biology in water
     */
    public static LocalBiology generateAbundanceBiologyExceptOnLand(
            GlobalBiology biology, SeaTile seaTile, HashMap<SeaTile, AbundanceBasedLocalBiology> locals) {
        if(seaTile.getAltitude() >= 0)
            return new EmptyLocalBiology();




        AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(biology);
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
        StockAssessmentCaliforniaMeristics meristics = new StockAssessmentCaliforniaMeristics(input);
        return new Species(speciesName, meristics);
    }

    /**
     * for a specific species create the natural processes object (which will be returned)
     * and register it as startable in the model
     * @param model the model
     * @param species the species you need the natural processes set up
     * @param locals a map of all the areas where fish can live
     * @param preserveLastAge
     * @param yearDelay
     * @param diffuser
     * @return the already scheduled naturalProcesses object
     */
    public static SingleSpeciesNaturalProcesses initializeNaturalProcesses(
            FishState model, Species species,
            Map<SeaTile, AbundanceBasedLocalBiology> locals,
            final boolean preserveLastAge, final int yearDelay,
            final NoAbundanceDiffusion diffuser) {
        //schedule recruitment and natural mortality
        AgingProcess agingProcess = new StandardAgingProcess(preserveLastAge);
        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(
                new NaturalMortalityProcess(),
                yearDelay > 0 ?
                        new RecruitmentBySpawningBiomassDelayed(
                                species.getVirginRecruits(),
                                species.getSteepness(),
                                species.getCumulativePhi(),
                                species.isAddRelativeFecundityToSpawningBiomass(),
                                yearDelay) :
                        new RecruitmentBySpawningBiomass(
                                species.getVirginRecruits(),
                                species.getSteepness(),
                                species.getCumulativePhi(),
                                species.isAddRelativeFecundityToSpawningBiomass()
                        ),
                species,
                agingProcess, diffuser);

        for (Map.Entry<SeaTile, AbundanceBasedLocalBiology> entry : locals.entrySet()) {
            processes.add(entry.getValue(),entry.getKey());
        }
        model.registerStartable(processes);
        return processes;
    }

    public int[][] getInitialAbundance (Species species)
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
                int[][] totalCount = factory.apply(model).getAbundance();
                initialAbundance.put(species,totalCount);

                //prepare the map biology-->ratio of fish to put there
                HashMap<AbundanceBasedLocalBiology, Double> currentWeightMap = new HashMap<>(locals.size());
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
                        model, species, locals, preserveLastAge, 0, new NoAbundanceDiffusion());
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
            Species species, HashMap<AbundanceBasedLocalBiology, Double> currentWeightMap) {
        Function<SeaTile, Double> allocator = allocators.get(species);
        Preconditions.checkArgument(allocator != null);
        //fill in the location
        for(Map.Entry<SeaTile,AbundanceBasedLocalBiology> local : locals.entrySet())
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
            Species speciesToReset, int[][] newTotalFishCount,
            HashMap<AbundanceBasedLocalBiology, Double> biologyToProportionOfFishThere)
    {
        if(speciesToReset.getName().equals(FAKE_SPECIES_NAME))
            return;

        HashMap<AbundanceBasedLocalBiology,Double> ratiosLocalCopy = new HashMap<>(biologyToProportionOfFishThere);
        for (Map.Entry<AbundanceBasedLocalBiology, Double> ratio : biologyToProportionOfFishThere.entrySet()) {
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
        for(Map.Entry<AbundanceBasedLocalBiology,Double> ratio : ratiosLocalCopy.entrySet())
        {

            //get the ratio back
            AbundanceBasedLocalBiology local = ratio.getKey();
            for(int i=0; i<=speciesToReset.getMaxAge(); i++)
            {

                local.getNumberOfMaleFishPerAge(speciesToReset)[i] =
                        (int) (0.5d + scaling * newTotalFishCount[FishStateUtilities.MALE][i] *ratio.getValue());
                local.getNumberOfFemaleFishPerAge(speciesToReset)[i] =
                        (int) (0.5d + scaling * newTotalFishCount[FishStateUtilities.FEMALE][i] *ratio.getValue());
            }
        }
        if(Log.DEBUG)
            Log.debug(speciesToReset + " resetted to total biomass: " +
                         locals.values().stream().mapToDouble(value -> value.getBiomass(speciesToReset)).sum());
    }

    /**
     * holds the weight given to each biology object when first created
     */
    private final HashMap<Species,HashMap<AbundanceBasedLocalBiology,Double>> initialWeights = new HashMap<>();





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
    public HashMap<AbundanceBasedLocalBiology,Double> getInitialWeights(Species species) {
        return initialWeights.get(species);
    }

    /**
     * Getter for property 'locals'.
     *
     * @return Value for property 'locals'.
     */
    public HashMap<SeaTile, AbundanceBasedLocalBiology> getLocals() {
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
