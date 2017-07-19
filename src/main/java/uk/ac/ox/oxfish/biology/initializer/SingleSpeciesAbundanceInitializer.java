package uk.ac.ox.oxfish.biology.initializer;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.*;
import uk.ac.ox.oxfish.biology.complicated.factory.InitialAbundanceFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.factory.MeristicsFileFactory;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.biology.initializer.allocator.ConstantBiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.ToDoubleFunction;

/**
 * A biology initializer that creates a one species model with abundance biology splitting the population equally
 * among all the seatiles
 * Created by carrknight on 3/11/16.
 */
public class SingleSpeciesAbundanceInitializer implements BiologyInitializer
{




    final private AlgorithmFactory<? extends InitialAbundance> initialAbundanceFactory;

    /**
     * object allocating biomass around when simulation starts
     */
    final private BiomassAllocator intialAbundanceAllocator;


    final private AgingProcess aging;


    /**
     * creates the weights/lengths of fish we are studying
     */
    final private Meristics meristics;

    /**
     * the name of the species
     */
    private final String speciesName;

    /**
     * scales down/up the number of fish present by multiplying it by scaling
     */
    private final double scaling;

    private final RecruitmentProcess recruitmentProcess;


    private final AbundanceDiffuser diffuser;

    /**
     * By default (if this is null) any area where there is no fish initially is marked as wasteland;
     * If this is not null, then even if there is no fish initially an area where this allocator returns > 0
     * will be livable
     *
     */
    final private BiomassAllocator habitabilityAllocator;


    /**
     * possibly null allocator to choose where recruits go
     */
    private final BiomassAllocator recruitmentAllocator;
    private SingleSpeciesNaturalProcesses processes;

    public SingleSpeciesAbundanceInitializer(
            String speciesName,
            AlgorithmFactory<? extends InitialAbundance> initialAbundanceFactory,
            BiomassAllocator intialAbundanceAllocator,
            AgingProcess aging,
            Meristics meristics,
            double scaling,
            RecruitmentProcess recruitmentProcess,
            AbundanceDiffuser diffuser,
            BiomassAllocator recruitmentAllocator,
            BiomassAllocator habitabilityAllocator) {
        this.initialAbundanceFactory = initialAbundanceFactory;
        this.intialAbundanceAllocator = intialAbundanceAllocator;
        this.aging = aging;
        this.meristics = meristics;
        this.speciesName = speciesName;
        this.scaling = scaling;
        this.recruitmentProcess = recruitmentProcess;
        this.diffuser = diffuser;
        this.recruitmentAllocator = recruitmentAllocator;
        this.habitabilityAllocator = habitabilityAllocator;
    }

    /**
     * list of all the abundance based local biologies
     */
    private HashMap<SeaTile,AbundanceBasedLocalBiology> locals = new HashMap<>();

    //allocate weights to each
    Map<AbundanceBasedLocalBiology, Double> initialWeights = new HashMap<>();


    /**
     * this is the default "read from files" california stock assessment constructor
     * @param biologicalDirectory
     * @param speciesName
     * @param scaling
     * @param state
     */
    public SingleSpeciesAbundanceInitializer(
            Path biologicalDirectory, String speciesName, double scaling, FishState state) {
        initialAbundanceFactory = new InitialAbundanceFromFileFactory(
                biologicalDirectory.resolve("count.csv"));
        meristics = new MeristicsFileFactory(
                biologicalDirectory.resolve("meristics.yaml")
        ).apply(state);
        recruitmentProcess = new RecruitmentBySpawningBiomass(
                meristics.getVirginRecruits(),
                meristics.getSteepness(),
                meristics.getCumulativePhi(),
                meristics.isAddRelativeFecundityToSpawningBiomass()
        );
        aging = new StandardAgingProcess(false);

        intialAbundanceAllocator = new ConstantBiomassAllocator();
        this.speciesName = speciesName;
        this.scaling = scaling;
        this.diffuser = new NoAbundanceDiffusion();
        this.recruitmentAllocator = null;
        this.habitabilityAllocator = null;
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


        if(seaTile.getAltitude() >= 0)
            return new EmptyLocalBiology();
        //weight we want to allocate to this area
        double weight = intialAbundanceAllocator.allocate(seaTile,
                                                          map,
                                                          random);
        //weights of 0 or below are wastelands
        if(weight <= 0 && (habitabilityAllocator == null || habitabilityAllocator.allocate(
                seaTile,
                map,
                random
        )  <= 0))
            return  new EmptyLocalBiology();
        else {
            AbundanceBasedLocalBiology local = new AbundanceBasedLocalBiology(biology);
            locals.put(seaTile,local);
            initialWeights.put(local, weight);
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
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model)
    {

        Preconditions.checkArgument(biology.getSize() == 1, "Single Species Abudance Initializer" +
                "used for multiple species");
        Species species = biology.getSpecie(0);

        //read in the total number of fish
        int[][] totalCount = initialAbundanceFactory.apply(model).getAbundance();
        assert totalCount.length == 2;
        Preconditions.checkArgument(totalCount[0].length == species.getMaxAge()+1,
                                    "mismatch between size of initial abundance and maxAge of species");


        //now the count is complete, let's distribute these fish uniformly throughout the seatiles


        double sum = initialWeights.values().stream().mapToDouble(
                new ToDoubleFunction<Double>() {
                    @Override
                    public double applyAsDouble(Double value) {
                        return value;
                    }
                }
        ).sum();



        for(Map.Entry<SeaTile,AbundanceBasedLocalBiology> local : locals.entrySet())
        {
            double ratio = initialWeights.get(local.getValue())/sum;

            for(int i=0; i<=species.getMaxAge(); i++)
            {

                local.getValue().getNumberOfMaleFishPerAge(species)[i] =
                        FishStateUtilities.randomRounding(
                                scaling * totalCount[FishStateUtilities.MALE][i] *ratio,
                                model.getRandom());
                local.getValue().getNumberOfFemaleFishPerAge(species)[i] =
                        FishStateUtilities.randomRounding(
                                scaling * totalCount[FishStateUtilities.FEMALE][i] *ratio,
                                model.getRandom());            }
        }



//        initializeNaturalProcesses(model, species, locals, false, 2);

        //create the natural process
        processes = new SingleSpeciesNaturalProcesses(
                new NaturalMortalityProcess(),
                recruitmentProcess,
                species,
                aging,
                diffuser
        );
        if(recruitmentAllocator !=null)
            processes.setRecruitsAllocator(recruitmentAllocator);
        //tell it to deal with our biologies
        for (Map.Entry<SeaTile, AbundanceBasedLocalBiology> entry : locals.entrySet()) {
            processes.add(entry.getValue(), entry.getKey());
        }
        //start it!
        model.registerStartable(processes);

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

        Species species = new Species(speciesName,meristics);
        return new GlobalBiology(species);

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
     * Getter for property 'processes'.
     *
     * @return Value for property 'processes'.
     */
    @VisibleForTesting
    public SingleSpeciesNaturalProcesses getProcesses() {
        return processes;
    }
}
