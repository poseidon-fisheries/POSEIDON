package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.AllocatorManager;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tries using as many separate objects as possible to keep biomass initialization easy;
 * Created by carrknight on 6/30/17.
 */
public class GenericBiomassInitializer extends AbstractBiologyInitializer
{

    private final List<DoubleParameter> carryingCapacity;


    private final DoubleParameter minInitialCapacity;

    private final DoubleParameter maxInitialCapacity;


    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;

    private final Map<SeaTile,BiomassLocalBiology> biologies =
            new HashMap<>();

    private final LogisticGrowerInitializer grower;

    /**
     * list of allocators (its size determines the number of species)
     */
    private final List<BiomassAllocator> allocators;

    private AllocatorManager manager;


    public GenericBiomassInitializer(
            List<DoubleParameter> carryingCapacity,
            DoubleParameter minInitialCapacity,
            DoubleParameter maxInitialCapacity, double percentageLimitOnDailyMovement,
            double differentialPercentageToMove,
            LogisticGrowerInitializer grower,
            List<BiomassAllocator> allocators) {
        this.carryingCapacity = carryingCapacity;
        this.minInitialCapacity = minInitialCapacity;
        this.maxInitialCapacity = maxInitialCapacity;
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
        this.grower = grower;
        this.allocators = allocators;
    }

    /**
     * Get a list of the species with their names. The size of this array determines the size of the model array
     *
     * @return
     */
    @Override
    public String[] getSpeciesNames() {
        String[] names = new String[allocators.size()];
        for(int i=0; i<allocators.size(); i++)
            names[i] = "Species " + i;
        return names;
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
            GlobalBiology biology, SeaTile seaTile,
            MersenneTwisterFast random, int mapHeightInCells,
            int mapWidthInCells, NauticalMap map) {

        //if there is no manager, we need to create and start it now
        if(manager == null)
        {
            manager = new AllocatorManager(false,
                                           allocators,
                                           biology);
            manager.start(map,random);
        }

        //create carrying capcities and put them in
        Double[] carringCapacities = new  Double[biology.getSize()];
        Double[] currentCapacity = new Double[biology.getSize()];
        for(Species species : biology.getSpecies())
        {
            Double k = carryingCapacity.get(species.getIndex()).apply(random) *
                    manager.getWeight(species, seaTile,map ,random );
            carringCapacities[species.getIndex()] = k;

            Double min = minInitialCapacity.apply(random);
            Double max = maxInitialCapacity.apply(random);
            currentCapacity[species.getIndex()] =
                    ((max - min)*random.nextDouble(true, true) + min)
                            * k;
        }
        BiomassLocalBiology local = new BiomassLocalBiology(currentCapacity, carringCapacities);
        biologies.put(seaTile,local);

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
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {

        grower.initializeGrower(biologies, model, random);


        BiomassDiffuser diffuser = new BiomassDiffuser(map, random, biology,
                                                       differentialPercentageToMove,
                                                       percentageLimitOnDailyMovement);

        model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);


    }
}
