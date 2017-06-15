package uk.ac.ox.oxfish.biology.initializer;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.growers.DerisoSchnuteCommonGrower;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * Created by carrknight on 6/14/17.
 */

public class MultipleSpeciesDerisoInitializer implements AllocatedBiologyInitializer
{
    public static final String FAKE_SPECIES_NAME = "Others";
    /**
     * the path to the biology folder, which must contain a count.csv and a meristic.yaml file
     */
    private final LinkedHashMap<String,Path> biologicalDirectories;


    /**
     * boolean representing whether or not we should add "others" as a mock species to account in the model
     * for everything else that is not directly modeled?
     */
    private final boolean addOtherSpecies;


    /**
     * Multiple species initializer for Deriso!
     * @param biologicalDirectories
     * @param addOtherSpecies
     */
    public MultipleSpeciesDerisoInitializer(
            LinkedHashMap<String, Path> biologicalDirectories,
            boolean addOtherSpecies) {
        this.biologicalDirectories = biologicalDirectories;
        this.addOtherSpecies = addOtherSpecies;
    }

    /**
     * defines the proportion of fish going to any sea-tile. No checks are made that the
     * proportions sum up to one so be careful!
     */
    private final HashMap<Species,
            Function<SeaTile, Double>> allocators = new HashMap<>();


    private final Map<SeaTile,BiomassLocalBiology> localBiologies = new HashMap<>();

    /**
     * contains all the mortality+recruitment processes of each species
     */
    private final HashMap<Species,DerisoSchnuteCommonGrower>
            naturalProcesses = new HashMap<>();

    /**
     * the deriso parameters for all the species
     */
    private final HashMap<Species, DerisoParameters> parameters = new HashMap<>();



    /**
     * read up a folder that contains deriso.yaml and turn it into a species
     * @param biologicalDirectory the folder containing meristics.yaml
     * @param speciesName the name of the species
     * @return the new species
     * @throws IOException
     */
    private Species generateSpeciesFromFolder(Path biologicalDirectory,
                                                    String speciesName)
            throws IOException {
        FishYAML yaml = new FishYAML();
        DerisoParameters parameter = yaml.loadAs(
                new FileReader(biologicalDirectory.resolve("deriso.yaml").toFile()),
                DerisoParameters.class);
        Species species = new Species(speciesName, Meristics.FAKE_MERISTICS);
        parameters.put(species,parameter);
        return species;
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
                                                  directory.getKey())
                );


            }
            //need to add an additional species to catch "all"
            if(addOtherSpecies)
                speciesList.add(new Species(FAKE_SPECIES_NAME,
                                            Meristics.FAKE_MERISTICS,
                                            true));


            return new GlobalBiology(speciesList.toArray(new Species[speciesList.size()]));
        }catch (IOException e) {
            e.printStackTrace();
            Log.error("Failed to instantiate the species because I couldn't find the meristics.yaml file in the folder provided");

        }
        System.exit(-1);
        return null;

    }

    /**
     * generates a bunch of empty local
     *
     * @param biology          the global biology (species' list) object
     * @param seaTile          the sea-tile to populate
     * @param random           the randomizer
     * @param mapHeightInCells height of the map
     * @param mapWidthInCells  width of the map
     */
    @Override
    public LocalBiology generateLocal(
            GlobalBiology biology, SeaTile seaTile, MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells) {


        if(seaTile.getAltitude() >= 0)
            return new EmptyLocalBiology();
        for(Species species : biology.getSpecies())
        {
            if(species.isImaginary())
                continue;
            //if at least one allocator says you have some fish here, then it's a logistic one
            if(allocators.get(species).apply(seaTile)>0)
            {
                BiomassLocalBiology local = new BiomassLocalBiology(0,biology.getSize(),random);
                localBiologies.put(seaTile,local);
                return local;
            }
        }
        //if you are here, all the allocations were 0!
        return new EmptyLocalBiology();


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
            for (Species species : biology.getSpecies()) {

                //if the species is the imaginary one, then ignore.
                if (addOtherSpecies && biologicalDirectories.get(species.getName()) == null) {
                    Preconditions.checkState(species.getName().equals(FAKE_SPECIES_NAME),
                                             "Do not have biological directory for species " + species.getName());
                    continue;
                }

                //we have a mapping tile---> weight
                //we want a mapping biology-->weight
                HashMap<BiomassLocalBiology, Double> weights =
                        new HashMap<>(localBiologies.size());


                Function<SeaTile, Double> allocator = allocators.get(species);
                Preconditions.checkArgument(allocator != null);
                //for every biology allocate it its weight (for this species)
                for (Map.Entry<SeaTile, BiomassLocalBiology> local : localBiologies.entrySet()) {
                    double ratio = allocator.apply(local.getKey());
                    weights.put(local.getValue(), ratio);
                }

                //we should have covered all locations by now
                assert localBiologies.values().containsAll(weights.keySet());
                assert weights.keySet().containsAll(localBiologies.values());

                //now assign biomass and carrying capacity to each biology
                DerisoParameters parameter = parameters.get(species);
                double virginBiomass = parameter.getVirginBiomass();
                double currentBiomass = parameter.getEmpiricalYearlyBiomasses().get(
                        parameter.getEmpiricalYearlyBiomasses().size());
                for (Map.Entry<BiomassLocalBiology, Double> bio : weights.entrySet()) {
                    assert bio.getKey().getCarryingCapacity(species) == 0;
                    assert bio.getKey().getBiomass(species) == 0;
                    bio.getKey().setCarryingCapacity(species,
                                                     bio.getValue() *
                                                             virginBiomass);
                    assert bio.getKey().getBiomass(species) == 0;
                    bio.getKey().setCurrentBiomass(species, currentBiomass);
                }

                //hopefully biomass sums up in the end!
                assert localBiologies.values().stream().mapToDouble(new ToDoubleFunction<BiomassLocalBiology>() {
                    @Override
                    public double applyAsDouble(BiomassLocalBiology value) {
                        return value.getCarryingCapacity(species);
                    }
                }).sum() == virginBiomass;
                assert localBiologies.values().stream().mapToDouble(new ToDoubleFunction<BiomassLocalBiology>() {
                    @Override
                    public double applyAsDouble(BiomassLocalBiology value) {
                        return value.getBiomass(species);
                    }
                }).sum() == currentBiomass;

                //schedule the damn grower!
                DerisoSchnuteCommonGrower grower = new DerisoSchnuteCommonGrower(
                        parameter.getEmpiricalYearlyBiomasses(),
                        parameter.getHistoricalYearlySurvival(),
                        parameter.getRho(),
                        parameter.getNaturalSurvivalRate(),
                        parameter.getRecruitmentSteepness(),
                        parameter.getRecruitmentLag(),
                        species.getIndex(),
                        parameter.getWeightAtRecruitment(),
                        parameter.getWeightAtRecruitmentMinus1(),
                        parameter.getLastRecruits()
                );
                model.registerStartable(grower);
                naturalProcesses.put(species,grower);

            }
            //done!
            biologicalDirectories.clear();
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.error("Failed to locate or read deriso parameters correctly. Could not instantiate the local biology");
            System.exit(-1);
        }
    }

    /**
     * puts the function describing the % of biomass that will initially be allocated to this sea-tile
     *
     * @param key
     * @param value
     */
    @Override
    public Function<SeaTile, Double> putAllocator(
            Species key, Function<SeaTile, Double> value) {
        return allocators.put(key, value);
    }
}
