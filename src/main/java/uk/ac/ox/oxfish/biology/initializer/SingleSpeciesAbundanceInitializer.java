package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
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
import java.util.LinkedList;
import java.util.List;

/**
 * A biology initializer that creates a one species model with abundance biology splitting the population equally
 * among all the seatiles
 * Created by carrknight on 3/11/16.
 */
public class SingleSpeciesAbundanceInitializer implements BiologyInitializer
{

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

    public SingleSpeciesAbundanceInitializer(Path biologicalDirectory, String speciesName, double scaling) {
        this.biologicalDirectory = biologicalDirectory;
        this.speciesName = speciesName;
        this.scaling = scaling;
    }

    /**
     * list of all the abundance based local biologies
     */
    private LinkedList<AbundanceBasedLocalBiology> locals = new LinkedList<>();

    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
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


        AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(biology);
        locals.add(local);
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

        assert biology.getSize() == 1;
        Species species = biology.getSpecie(0);
        int maxAge = species.getMaxAge();
        int[][] totalCount = new int[2][maxAge +1];
        //read in the total number of fish
        try {
            List<String> countfile = Files.readAllLines(biologicalDirectory.resolve("count.csv"));
            assert  countfile.size() == maxAge +2; //it's the count + title line
            String[] titleLine = countfile.get(0).split(",");
            //expect to be female and then male
            Preconditions.checkArgument(titleLine.length == 2);
            Preconditions.checkArgument(titleLine[0].trim().toLowerCase().equals("female"));
            Preconditions.checkArgument(titleLine[1].trim().toLowerCase().equals("male"));

            for(int i=1;i<countfile.size(); i++)
            {
                String[] line = countfile.get(i).split(","); //this is not very efficient but it's a 100 lines at most so no big deal
                assert  line.length == 2;
                totalCount[FishStateUtilities.FEMALE][i-1] = Integer.parseInt(line[0]);
                totalCount[FishStateUtilities.MALE][i-1] = Integer.parseInt(line[1]);
            }


            //now the count is complete, let's distribute these fish uniformly throughout the seatiles
            double tiles = locals.size();
            for(AbundanceBasedLocalBiology local : locals)
            {
                for(int i=0; i<=maxAge; i++)
                {
                    local.getNumberOfMaleFishPerAge(species)[i] = (int) (0.5d + scaling * totalCount[FishStateUtilities.MALE][i] /tiles);
                    local.getNumberOfFemaleFishPerAge(species)[i] = (int) (0.5d + scaling * totalCount[FishStateUtilities.FEMALE][i] /tiles);
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            Log.error("Failed to locate or read count.csv correctly. Could not instantiate the local biology");
            System.exit(-1);
        }

        //schedule recruitment and natural mortality
        NaturalProcesses processes = new NaturalProcesses(
                new NaturalMortalityProcess(),
                new RecruitmentBySpawningBiomassDelayed(
                        species.getVirginRecruits(),
                        species.getSteepness(),
                        species.isAddRelativeFecundityToSpawningBiomass(),
                        2
                )
        );
        for(AbundanceBasedLocalBiology local : locals)
            processes.add(local);
        processes.start(model);


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

        FishYAML yaml = new FishYAML();
        try {
            String meristicFile = String.join("\n", Files.readAllLines(biologicalDirectory.resolve("meristics.yaml")));
            MeristicsInput input = yaml.loadAs(meristicFile,MeristicsInput.class);
            Meristics meristics = new Meristics(input);
            return new GlobalBiology(new Species(speciesName,meristics));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.error("Failed to instantiate the species because I couldn't find the meristics.yaml file in the folder provided");
        System.exit(-1);
        return null;
    }
}
