/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import org.jfree.util.Log;
import uk.ac.ox.oxfish.biology.BiomassDiffuserContainer;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.BiomassMovementRule;
import uk.ac.ox.oxfish.biology.ConstantBiomassDecorator;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.biology.initializer.allocator.AllocatorManager;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static tech.units.indriya.unit.Units.KILOGRAM;

public class SingleSpeciesBiomassInitializer implements BiologyInitializer{



    final private InitialBiomass initialTotalBiomass;

    /**
     * tells me where to place initial biomass; it's renormalized
     */
    final private BiomassAllocator initialAllocator;

    final private InitialBiomass totalCapacity;

    /**
     * All 0 and negative numbers of this mark the wastelands.
     * If normalized, this will the portion of totalCapacity used for each cell.
     * If not normalized this will be the carrying capacity per cell
     */
    final private BiomassAllocator carryingCapacityAllocator;




    final private BiomassMovementRule movementRule;


    final private String speciesName;

    @NotNull
    final private LogisticGrowerInitializer grower;


    /**
     * when this is true (default), the allocators are normalized to sum up to 1.
     * When this is false the allocators can be anything, if you couple not-normalized allocators with initialBiomass of 1, then
     * the all biomass numbers are provided by the allocator itself
     */
    final private boolean normalizeAllocators;




    /**
     * you can set this true and biomass diffuser won't be generated when processing the map
     */
    private boolean forceMovementOff = false;


    private boolean hasAlreadyWarned = false;



    private boolean unfishable;


    public SingleSpeciesBiomassInitializer(
            InitialBiomass initialTotalBiomass,
            BiomassAllocator initialAllocator, InitialBiomass totalCapacity,
            BiomassAllocator carryingCapacityAllocator, BiomassMovementRule movementRule,
            String speciesName,
            @NotNull LogisticGrowerInitializer grower,
            boolean normalizeAllocators, boolean unfishable) {
        this.initialTotalBiomass = initialTotalBiomass;
        this.initialAllocator = initialAllocator;
        this.totalCapacity = totalCapacity;
        this.carryingCapacityAllocator = carryingCapacityAllocator;
        this.movementRule = movementRule;

        this.speciesName = speciesName;
        this.grower = grower;
        this.normalizeAllocators = normalizeAllocators;
        this.unfishable = unfishable;
    }


    /**
     * this constructor assumes that the biomassAllocators will not be normalized and their output
     * provides the raw amount of biomass available/carrying capacity
     * @param initialAllocator
     * @param carryingCapacityAllocator
     * @param speciesName
     * @param grower
     * @param unfishable
     */
    public SingleSpeciesBiomassInitializer(
            BiomassAllocator initialAllocator,
            BiomassAllocator carryingCapacityAllocator,
            BiomassMovementRule movementRule,
            String speciesName,
            @NotNull LogisticGrowerInitializer grower, boolean unfishable) {
        this.unfishable = unfishable;
        this.initialTotalBiomass = new ConstantInitialBiomass(1);
        this.totalCapacity = new ConstantInitialBiomass(1);


        this.initialAllocator = initialAllocator;
        this.carryingCapacityAllocator = carryingCapacityAllocator;
        this.movementRule = movementRule;
        this.speciesName = speciesName;
        this.grower = grower;
        this.normalizeAllocators = false;
    }

    //helpers, instantiated when "globalBiology" is called
    private AllocatorManager initialDistribution;
    private AllocatorManager habitabilityDistribution;


    private int numberOfHabitableCells = 0;
    /**
     * this gets called for each tile by the map as the tile is created. Do not expect it to come in order
     *
     * @param biology          the global biology (species' list) object
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
        //we are going to work on a single species!
        Species species = biology.getSpecie(speciesName);

        if(!initialDistribution.isStarted()) {
            habitabilityDistribution.start(map, random);

            //if we are dealing with normalized allocators, it's a good idea to
            //zero out the weights for areas where there can be no fish
            if(normalizeAllocators)
            {
                SeaTile tile = map.getAllSeaTilesExcludingLandAsList().iterator().next();
                double weight = habitabilityDistribution.getWeight(
                        species,
                        tile,
                        map,
                        random
                );
                if(Double.isNaN(weight) || weight<=0)
                    initialDistribution.getZeroedArea().add(tile);
            }

            initialDistribution.start(map, random);
        }



        double habitability = habitabilityDistribution.getWeight(species,
                seaTile,
                map,
                random);


        //we return an empty biology object. We will fill it in the processing phase
        if(habitability <=0 || !Double.isFinite(habitability))
            return new EmptyLocalBiology();
        else{
            numberOfHabitableCells++;
            double[] currentBiomass = new double[biology.getSize()];
            double[] carryingCapacity = new double[biology.getSize()];
            Arrays.fill(currentBiomass,0d);
            Arrays.fill(carryingCapacity,0d);
            return new BiomassLocalBiology(currentBiomass,
                    carryingCapacity);


        }




    }

    /**
     * after all the tiles have been instantiated this method gets called once to fill in the biomasses
     *
     * @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random
     * @param model   the model: it is in the process of being initialized so it should be only used to schedule
     *                stuff rather
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {
        //we are going to work on a single species!
        Species species = biology.getSpecie(speciesName);


        //generate correct numbers of starting biomass!
        double totalCarryingCapacity = totalCapacity.getInitialBiomass(
                map,
                species,
                numberOfHabitableCells
        ) ;
        double totalStartingBiomass = initialTotalBiomass.getInitialBiomass(
                map,
                species,
                numberOfHabitableCells
        ) ;

        Map<SeaTile,BiomassLocalBiology> habitableAreas = new HashMap<>();
        //for each tile of the map
        for(SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList())
        {

            double habitability = habitabilityDistribution.getWeight(species,
                    seaTile,
                    map,
                    random);

            //don't bother if you can't live there
            if(habitability<=0 || Double.isNaN(habitability))
                continue;


            //create and assign amount of fish available initially
            double carryingCapacity = totalCarryingCapacity * habitability;
            double startingBiomass;
            if(!normalizeAllocators)
                startingBiomass = totalStartingBiomass *
                        initialDistribution.getWeight(species,
                                seaTile,
                                map,
                                random);
            //when normalizing, the initialDistribution only tells us the proportion of carrying capacity present
            else
                startingBiomass = initialDistribution.getWeight(species,
                        seaTile,
                        map,
                        random) * carryingCapacity;

            if(startingBiomass<=0 || Double.isNaN(startingBiomass))
                startingBiomass = 0;

            //if inconsistent, carrying capacity limits initial biomass!
            if(startingBiomass > carryingCapacity) {
                startingBiomass = carryingCapacity;
                if(!hasAlreadyWarned)
                {
                    Log.warn("Initialized a cell with more initial biomass than carrying capacity; reduced initial biomass to current capacity");
                    hasAlreadyWarned = true;
                }
            }
            BiomassLocalBiology local = (BiomassLocalBiology) seaTile.getBiology();
            local.setCarryingCapacity(
                    species,
                    carryingCapacity
            );
            local.setCurrentBiomass(
                    species,
                    startingBiomass
            );
            habitableAreas.put(seaTile,local);

            Preconditions.checkArgument(startingBiomass<=carryingCapacity,
                    "carrying capacity allocated less than initial biomass allocated! ");

            if(unfishable)
                seaTile.setBiology(new ConstantBiomassDecorator(local));

        }
        Preconditions.checkArgument(habitableAreas.size()==numberOfHabitableCells,
                "failure at computing the correct number of habitable cells");


        //initialize the grower
        grower.initializeGrower(habitableAreas, model, random,species);
        //initialize the diffuser
        if(!forceMovementOff) {
            BiomassDiffuserContainer diffuser = new BiomassDiffuserContainer(map, random, biology,
                    new Pair<>(
                            species,
                            movementRule
                    )
            );
            model.scheduleEveryDay(diffuser, StepOrder.BIOLOGY_PHASE);
        }



    }

    /**
     * returns a one species object
     *
     * @param random                the random number generator
     * @param modelBeingInitialized the model we are in the process of initializing
     * @return a global biology object
     */
    @Override
    public GlobalBiology generateGlobal(
            MersenneTwisterFast random, FishState modelBeingInitialized) {


        Species species = new Species(speciesName);
        GlobalBiology independentGlobalBiology = new GlobalBiology(species);
        //create maps of where the fish is
        initialDistribution = new AllocatorManager(
                false,
                species,
                initialAllocator,
                independentGlobalBiology
        );
        habitabilityDistribution = new AllocatorManager(
                normalizeAllocators,
                species,
                carryingCapacityAllocator,
                independentGlobalBiology
        );

        final String columnName = species + " Recruitment";
        modelBeingInitialized.getYearlyCounter().addColumn(columnName);
        modelBeingInitialized.getYearlyDataSet().registerGatherer(
            columnName,
            state -> modelBeingInitialized.getYearlyCounter().getColumn(columnName),
            0d,
            KILOGRAM,
            "Biomass"
        );

        return independentGlobalBiology;

    }


    /**
     * Getter for property 'initialTotalBiomass'.
     *
     * @return Value for property 'initialTotalBiomass'.
     */
    public InitialBiomass getInitialTotalBiomass() {
        return initialTotalBiomass;
    }

    /**
     * Getter for property 'initialAllocator'.
     *
     * @return Value for property 'initialAllocator'.
     */
    public BiomassAllocator getInitialAllocator() {
        return initialAllocator;
    }

    /**
     * Getter for property 'totalCapacity'.
     *
     * @return Value for property 'totalCapacity'.
     */
    public InitialBiomass getTotalCapacity() {
        return totalCapacity;
    }

    /**
     * Getter for property 'carryingCapacityAllocator'.
     *
     * @return Value for property 'carryingCapacityAllocator'.
     */
    public BiomassAllocator getCarryingCapacityAllocator() {
        return carryingCapacityAllocator;
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
     * Getter for property 'grower'.
     *
     * @return Value for property 'grower'.
     */
    @NotNull
    public LogisticGrowerInitializer getGrower() {
        return grower;
    }

    /**
     * Getter for property 'movementRule'.
     *
     * @return Value for property 'movementRule'.
     */
    public BiomassMovementRule getMovementRule() {
        return movementRule;
    }

    /**
     * Getter for property 'forceMovementOff'.
     *
     * @return Value for property 'forceMovementOff'.
     */
    public boolean isForceMovementOff() {
        return forceMovementOff;
    }

    /**
     * Setter for property 'forceMovementOff'.
     *
     * @param forceMovementOff Value to set for property 'forceMovementOff'.
     */
    public void setForceMovementOff(boolean forceMovementOff) {
        this.forceMovementOff = forceMovementOff;
    }

    /**
     * Getter for property 'unfishable'.
     *
     * @return Value for property 'unfishable'.
     */
    public boolean isUnfishable() {
        return unfishable;
    }

    /**
     * Setter for property 'unfishable'.
     *
     * @param unfishable Value to set for property 'unfishable'.
     */
    public void setUnfishable(boolean unfishable) {
        this.unfishable = unfishable;
    }
}
