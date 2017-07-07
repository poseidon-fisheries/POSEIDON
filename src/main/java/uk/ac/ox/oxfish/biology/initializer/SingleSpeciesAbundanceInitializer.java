package uk.ac.ox.oxfish.biology.initializer;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A biology initializer that creates a one species model with abundance biology splitting the population equally
 * among all the seatiles
 * Created by carrknight on 3/11/16.
 */
public class SingleSpeciesAbundanceInitializer implements BiologyInitializer
{

    private static StandardAgingProcess agingProcess;
    /**
     * the path to the biology folder, which must contain a count.csv and a meristic.yaml file
     */
    private final Path biologicalDirectory;

    /**
     * the name of the species
     */
    private final String speciesName;

    /**
     * scales down the number of fish present by multiplying it by scaling
     */
    private final double scaling;


    /**
     * list of all the abundance based local biologies
     */
    private HashMap<SeaTile,AbundanceBasedLocalBiology> locals = new HashMap<>();


    public SingleSpeciesAbundanceInitializer(
            Path biologicalDirectory, String speciesName, double scaling) {
        this.biologicalDirectory = biologicalDirectory;
        this.speciesName = speciesName;
        this.scaling = scaling;
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
        return generateAbundanceBiologyExceptOnLand(biology, seaTile, locals);
    }

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
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {

        Preconditions.checkArgument(biology.getSize() == 1, "Single Species Abudance Initializer" +
                "used for multiple species");
        Species species = biology.getSpecie(0);

        //read in the total number of fish
        try {
            int[][] totalCount = turnCountsFileIntoAbundanceArray(species, biologicalDirectory);


            //now the count is complete, let's distribute these fish uniformly throughout the seatiles
            double tiles = locals.size();
            for(Map.Entry<SeaTile,AbundanceBasedLocalBiology> local : locals.entrySet())
            {
                double ratio = 1d/tiles;

                for(int i=0; i<=species.getMaxAge(); i++)
                {

                    local.getValue().getNumberOfMaleFishPerAge(species)[i] =
                            (int) (0.5d + scaling * totalCount[FishStateUtilities.MALE][i] *ratio);
                    local.getValue().getNumberOfFemaleFishPerAge(species)[i] =
                            (int) (0.5d + scaling * totalCount[FishStateUtilities.FEMALE][i] *ratio);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            Log.error("Failed to locate or read count.csv correctly. Could not instantiate the local biology");
            System.exit(-1);
        }
        initializeNaturalProcesses(model, species, locals, false, 2);


    }

    /**
     * for a specific species create the natural processes object (which will be returned)
     * and register it as startable in the model
     * @param model the model
     * @param species the species you need the natural processes set up
     * @param locals a map of all the areas where fish can live
     * @param preserveLastAge
     * @param yearDelay
     * @return the already scheduled naturalProcesses object
     */
    public static SingleSpeciesNaturalProcesses initializeNaturalProcesses(
            FishState model, Species species,
            Map<SeaTile,AbundanceBasedLocalBiology> locals,
            final boolean preserveLastAge, final int yearDelay) {
        //schedule recruitment and natural mortality
        agingProcess = new StandardAgingProcess(preserveLastAge);
        SingleSpeciesNaturalProcesses processes = new SingleSpeciesNaturalProcesses(
                new NaturalMortalityProcess(),
                yearDelay > 0 ?
                new RecruitmentBySpawningBiomassDelayed(
                        species.getVirginRecruits(),
                        species.getSteepness(),
                        species.isAddRelativeFecundityToSpawningBiomass(),
                        yearDelay
                ) :
                new RecruitmentBySpawningBiomass(
                        species.getVirginRecruits(),
                        species.getSteepness(),
                        species.isAddRelativeFecundityToSpawningBiomass()
                ),
                species,
                agingProcess);

        for (Map.Entry<SeaTile, AbundanceBasedLocalBiology> entry : locals.entrySet()) {
            processes.add(entry.getValue(),entry.getKey());
        }
        model.registerStartable(processes);
        return processes;
    }

    /**
     * read count.csv in the biological directory and turn it into an array representing total fish
     * abundance
     * @param species the species object
     * @param biologicalDirectory the biological directory
     * @return the array containing the count split into age cohorts
     * @throws IOException failed to read the file
     */
    public static int[][] turnCountsFileIntoAbundanceArray(
            Species species, Path biologicalDirectory) throws IOException {
        if(Log.TRACE)
            Log.trace("Reading up " + biologicalDirectory);
        int maxAge = species.getMaxAge();
        int[][] totalCount = new int[2][maxAge +1];
        List<String> countfile = Files.readAllLines(biologicalDirectory.resolve("count.csv"));
        assert  countfile.size() >= maxAge +2; //it's the count + title line
        //allow for one line of empty space
        assert  countfile.size() <= maxAge +3 : "there is more than one empty space at the end of count.csv or just too many rows";
        String[] titleLine = countfile.get(0).split(",");
        //expect to be female and then male
        Preconditions.checkArgument(titleLine.length == 2);
        Preconditions.checkArgument(titleLine[0].trim().toLowerCase().equals("female"));
        Preconditions.checkArgument(titleLine[1].trim().toLowerCase().equals("male"));

        for(int i=1;i<maxAge +2; i++)
        {
            String[] line = countfile.get(i).split(","); //this is not very efficient but it's a 100 lines at most so no big deal
            assert  line.length == 2;
            totalCount[FishStateUtilities.FEMALE][i-1] = Integer.parseInt(line[0]);
            totalCount[FishStateUtilities.MALE][i-1] = Integer.parseInt(line[1]);
        }
        return totalCount;
    }

    /**
     * creates the global biology object for the model
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(
            MersenneTwisterFast random, FishState modelBeingInitialized) {

        try {
            Species species = generateSpeciesFromFolder(biologicalDirectory, speciesName);
            return new GlobalBiology(species);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.error("Failed to instantiate the species because I couldn't find the meristics.yaml file in the folder provided");
        System.exit(-1);
        return null;
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
     * Getter for property 'biologicalDirectory'.
     *
     * @return Value for property 'biologicalDirectory'.
     */
    public Path getBiologicalDirectory() {
        return biologicalDirectory;
    }

    /**
     * Getter for property 'speciesName'.
     *
     * @return Value for property 'speciesName'.
     */
    public String getSpeciesName() {
        return speciesName;
    }

    /**
     * Getter for property 'scaling'.
     *
     * @return Value for property 'scaling'.
     */
    public double getScaling() {
        return scaling;
    }


    /**
     * Getter for property 'agingProcess'.
     *
     * @return Value for property 'agingProcess'.
     */
    public static StandardAgingProcess getAgingProcess() {
        return agingProcess;
    }

    /**
     * Setter for property 'agingProcess'.
     *
     * @param agingProcess Value to set for property 'agingProcess'.
     */
    public static void setAgingProcess(StandardAgingProcess agingProcess) {
        SingleSpeciesAbundanceInitializer.agingProcess = agingProcess;
    }
}
