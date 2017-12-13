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
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.complicated.DummyNaturalMortality;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.biology.growers.DerisoSchnuteCommonGrower;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.io.IOException;
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
    private String derisoYamlFileName = "deriso.yaml";


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
    private final LinkedHashMap<Species,
            Function<SeaTile, Double>> allocators = new LinkedHashMap<>();


    private final LinkedHashMap<SeaTile,BiomassLocalBiology> localBiologies = new LinkedHashMap<>();

    /**
     * contains all the mortality+recruitment processes of each species
     */
    private final LinkedHashMap<Species,DerisoSchnuteCommonGrower>
            naturalProcesses = new LinkedHashMap<>();

    /**
     * the deriso parameters for all the species
     */
    private final LinkedHashMap<Species, DerisoParameters> parameters = new LinkedHashMap<>();


    /**
     * stored and used during reset only!
     */
    private LinkedHashMap<Species,LinkedHashMap<BiomassLocalBiology,Double>> originalWeights = new LinkedHashMap<>();



    private LinkedHashMap<Species,Double> movementRate = new LinkedHashMap<>();

    /**
     * read up a folder that contains deriso.yaml and turn it into a species
     * @param biologicalDirectory the folder containing deriso.yaml
     * @param speciesName the name of the species
     * @return the new species
     * @throws IOException
     */
    private Species generateSpeciesFromFolder(Path biologicalDirectory,
                                              String speciesName)
            throws IOException {
        FishYAML yaml = new FishYAML();
        FileReader io = new FileReader(biologicalDirectory.resolve(derisoYamlFileName).toFile());
        DerisoParameters parameter = yaml.loadAs(
                io,
                DerisoParameters.class);
        io.close();
        parameter.updateLastRecruits();
        Species species = new Species(speciesName);
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
                                            StockAssessmentCaliforniaMeristics.FAKE_MERISTICS,
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


        if(seaTile.getAltitude() >= 0)
            return new EmptyLocalBiology();
        else
        {

            BiomassLocalBiology local = new BiomassLocalBiology(0,biology.getSize(),random);
            localBiologies.put(seaTile,local);
            return local;

        }



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
                LinkedHashMap<BiomassLocalBiology, Double> weights =
                        new LinkedHashMap<>(localBiologies.size());


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
                originalWeights.put(species,weights);

                //now assign biomass and carrying capacity to each biology
                resetLocalBiology(species, weights);
                DerisoParameters parameter;
                double virginBiomass;
                double currentBiomass;


                //scale virgin biomass/empirical biomasses now
                parameter = parameters.get(species);
                double totalWeight = weights.values().stream().mapToDouble(value -> value).sum();
                virginBiomass = parameter.getVirginBiomass() * totalWeight;
                LinkedList<Double> scaledEmpiricalBiomasses = new LinkedList<>(parameter.getEmpiricalYearlyBiomasses());
                scaledEmpiricalBiomasses.replaceAll(original -> original * totalWeight);
                currentBiomass = scaledEmpiricalBiomasses.get(scaledEmpiricalBiomasses.size()-1);

                //hopefully biomass sums up in the end!

                assert Math.abs(localBiologies.values().stream().mapToDouble(new ToDoubleFunction<BiomassLocalBiology>() {
                    @Override
                    public double applyAsDouble(BiomassLocalBiology value) {
                        return value.getCarryingCapacity(species);
                    }
                }).sum() -virginBiomass) < FishStateUtilities.EPSILON;
                assert Math.abs(localBiologies.values().stream().mapToDouble(new ToDoubleFunction<BiomassLocalBiology>() {
                    @Override
                    public double applyAsDouble(BiomassLocalBiology value) {
                        return value.getBiomass(species);
                    }
                }).sum() - currentBiomass)<.01;


                parameter.updateLastRecruits();

                //schedule the damn grower!
                DerisoSchnuteCommonGrower grower = new DerisoSchnuteCommonGrower(
                        scaledEmpiricalBiomasses,
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
                //register all valid biologies to be grown
                for (Map.Entry<SeaTile, BiomassLocalBiology> bio : localBiologies.entrySet()) {
                    if(bio.getValue().getCarryingCapacity(species)>0)
                        grower.getBiologies().add(bio.getValue());
                }


                model.registerStartable(grower);
                naturalProcesses.put(species,grower);

            }
            //clear out all empty biologies
            List<SeaTile> toClear = new LinkedList<>();
            for (Map.Entry<SeaTile, BiomassLocalBiology> bio : localBiologies.entrySet())
            {
                double sum = 0;
                for(int i=0; i<biology.getSize(); i++)
                    sum += bio.getValue().getCarryingCapacity(i);
                if(sum<=0)
                {
                    bio.getKey().setBiology(new EmptyLocalBiology());
                    toClear.add(bio.getKey());
                }
            }
            for(SeaTile bio : toClear)
                localBiologies.remove(bio);


            //movement rates
            for (Map.Entry<Species, Double> movement : movementRate.entrySet()) {
                assert movement.getValue()>0;
                BiomassDiffuser diffuser = new BiomassDiffuser(map, random, biology, movement.getValue(), .001d,
                                                               movement.getKey().getIndex());

                model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);
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
     * you must at all time be ready to reset local biology to its pristine state
     *
     * @param species species you want the biomass resetted
     */
    @Override
    public void resetLocalBiology(Species species) {
        resetLocalBiology(species,originalWeights.get(species));
    }

    /**
     * allocates biomass and carrying capacity all around
     * @param species
     * @param weights
     */
    private void resetLocalBiology(Species species, LinkedHashMap<BiomassLocalBiology, Double> weights) {
        if(species.isImaginary())
            return;
        DerisoParameters parameter = parameters.get(species);
        double virginBiomass = parameter.getVirginBiomass();
        double currentBiomass = parameter.getEmpiricalYearlyBiomasses().get(
                parameter.getEmpiricalYearlyBiomasses().size()-1);
        for (Map.Entry<BiomassLocalBiology, Double> bio : weights.entrySet()) {
           // these asserts are only true the first time you call this method
            // assert bio.getKey().getCarryingCapacity(species) == 0;
           // assert bio.getKey().getBiomass(species) == 0;
            bio.getKey().setCarryingCapacity(species,
                                             weights.get(bio.getKey()) *
                                                     virginBiomass);
           // assert bio.getKey().getBiomass(species) == 0;
            bio.getKey().setCurrentBiomass(species, weights.get(bio.getKey()) * currentBiomass);
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

    @VisibleForTesting
    public HashMap<Species, DerisoSchnuteCommonGrower> getNaturalProcesses() {
        return naturalProcesses;
    }

    /**
     * Getter for property 'movementRate'.
     *
     * @return Value for property 'movementRate'.
     */
    public HashMap<Species, Double> getMovementRate() {
        return movementRate;
    }

    /**
     * Setter for property 'movementRate'.
     *
     * @param movementRate Value to set for property 'movementRate'.
     */
    public void setMovementRate(LinkedHashMap<Species, Double> movementRate) {
        this.movementRate = movementRate;
    }


    /**
     * Getter for property 'derisoYamlFileName'.
     *
     * @return Value for property 'derisoYamlFileName'.
     */
    public String getDerisoYamlFileName() {
        return derisoYamlFileName;
    }

    /**
     * Setter for property 'derisoYamlFileName'.
     *
     * @param derisoYamlFileName Value to set for property 'derisoYamlFileName'.
     */
    public void setDerisoYamlFileName(String derisoYamlFileName) {
        this.derisoYamlFileName = derisoYamlFileName;
    }
}
