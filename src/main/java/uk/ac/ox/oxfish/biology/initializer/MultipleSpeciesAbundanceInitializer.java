package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceBasedLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

/**
 * Created by carrknight on 3/17/16.
 */
public class MultipleSpeciesAbundanceInitializer implements BiologyInitializer
{

    /**
     * the path to the biology folder, which must contain a count.csv and a meristic.yaml file
     */
    private final LinkedHashMap<String,Path> biologicalDirectories;


    /**
     * scales down the number of fish present by multiplying it by scaling
     */
    private final double scaling;


    public MultipleSpeciesAbundanceInitializer(
            LinkedHashMap<String, Path> biologicalDirectories, double scaling) {
        this.biologicalDirectories = biologicalDirectories;
        this.scaling = scaling;
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
        return SingleSpeciesAbundanceInitializer.generateAbundanceBiologyExceptOnLand(biology,seaTile,
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
                speciesList.add(SingleSpeciesAbundanceInitializer.
                        generateSpeciesFromFolder(directory.getValue(),
                                                  directory.getKey()));


            }
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
            for (Species species : biology.getSpecies()) {
                int[][] totalCount = SingleSpeciesAbundanceInitializer.
                        turnCountsFileIntoAbundanceArray(species, biologicalDirectories.get(species.getName()));

                //now allocate the count correctly
                Function<SeaTile, Double> allocator = allocators.get(species);
                Preconditions.checkArgument(allocator != null);

                //for each tile-biology
                for(Map.Entry<SeaTile,AbundanceBasedLocalBiology> local : locals.entrySet())
                {

                    //find the ratio by allocator
                    double ratio = allocator.apply(local.getKey());

                    for(int i=0; i<=species.getMaxAge(); i++)
                    {

                        local.getValue().getNumberOfMaleFishPerAge(species)[i] =
                                (int) (0.5d + scaling * totalCount[FishStateUtilities.MALE][i] *ratio);
                        local.getValue().getNumberOfFemaleFishPerAge(species)[i] =
                                (int) (0.5d + scaling * totalCount[FishStateUtilities.FEMALE][i] *ratio);
                    }
                }


                SingleSpeciesAbundanceInitializer.initializeNaturalProcesses(model, species, locals);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.error("Failed to locate or read count.csv correctly. Could not instantiate the local biology");
            System.exit(-1);
        }


    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public Function<SeaTile, Double> putAllocator(
            Species key,
            Function< SeaTile, Double> value) {
        return allocators.put(key, value);
    }

    public int getNumberOfFishableTiles(){
        return locals.size();
    }
}
