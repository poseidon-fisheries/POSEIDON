package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.ArrayList;
import java.util.List;

/**
 * the biomass of the whole map is aggregated and spawns logistically.
 * The fish is reallocated at random
 */
public class CommonLogisticGrower implements Startable, Steppable {

    /**
     * the uninpeded growth rate of each species
     */
    private final double malthusianParameter;
    private Stoppable receipt;

    private final Species species;

    private final boolean distributeProportionally;

    public CommonLogisticGrower(double malthusianParameter, Species species, boolean distributeProportionally) {
        this.malthusianParameter = malthusianParameter;
        this.species = species;
        this.distributeProportionally = distributeProportionally;
    }

    /**
     * list of biologies to grow. You can use a single grower for all the cells or a separate grower
     * for each cell. It shouldn't be too much of a big deal.
     */
    private final List<BiomassLocalBiology> biologies = new ArrayList<>();


    @Override
    public void step(SimState simState) {

        FishState model = ((FishState) simState);

        double current = 0;
        double capacity = 0;
        //for each place
        for(VariableBiomassBasedBiology biology : biologies)
        {
            current += biology.getBiomass(species);
            capacity +=biology.getCarryingCapacity(species);

        }
        double recruitment = IndependentLogisticBiomassGrower.logisticRecruitment(
                current,
                capacity,
                malthusianParameter

        );
        //compute recruitment
        recruitment = Math.min(recruitment,capacity-current);
        assert recruitment>=-FishStateUtilities.EPSILON;
        if(recruitment>FishStateUtilities.EPSILON) {
            //distribute it
            if(distributeProportionally)
                CommonLogisticGrower.allocateBiomassProportionally(biologies,
                        recruitment,
                        model.getRandom(),
                        species.getIndex());
            else
                DerisoSchnuteCommonGrower.allocateBiomassAtRandom(
                        biologies,
                        recruitment,
                        model.getRandom(),
                        species.getIndex()
                );
            //count growth
            model.getYearlyCounter().count(model.getSpecies().get(species.getIndex()) +
                            " Recruitment",
                    recruitment);
        }




        if(biologies.size()==0) //if you removed all the biologies then we are done
            turnOff();
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model)
    {
        //schedule yourself
        Preconditions.checkArgument(receipt==null, "Already started!");
        receipt = model.scheduleEveryYear(this, StepOrder.BIOLOGY_PHASE);
    }


    public List<BiomassLocalBiology> getBiologies() {
        return biologies;
    }

    @Override
    public void turnOff() {
        biologies.clear();
    }

    /**
     * allocates recruitment to each cell in proportion of what's missing there (in absolute terms)
     * @param biologyList list of cells that can be repopulation
     * @param toReallocate biomass growth
     * @param random randomizer
     * @param speciesIndex the species number
     */
    public static void allocateBiomassProportionally(List<? extends VariableBiomassBasedBiology> biologyList,
                                                     double toReallocate,
                                                     MersenneTwisterFast random, int speciesIndex){

        if(toReallocate<FishStateUtilities.EPSILON) //don't bother with super tiny numbers
            return;
        double totalEmptySpace = 0;
        for (VariableBiomassBasedBiology local : biologyList) {
            totalEmptySpace += local.getCarryingCapacity(speciesIndex) - local.getCurrentBiomass()[speciesIndex];
        }
        //if there is less empty space than recruitment, just fill it all up!
        if(totalEmptySpace<=toReallocate) {
            for (VariableBiomassBasedBiology local : biologyList) {
                local.getCurrentBiomass()[speciesIndex] = local.getCarryingCapacity(speciesIndex);
            }
        }
        else
        {
            //reallocate proportional to the empty space here compared to the total empty space
            for (VariableBiomassBasedBiology local : biologyList) {
                double emptySpace = local.getCarryingCapacity(speciesIndex) -
                        local.getCurrentBiomass()[speciesIndex];
                local.getCurrentBiomass()[speciesIndex] += toReallocate * emptySpace/totalEmptySpace;
                assert local.getCurrentBiomass()[speciesIndex] <= local.getCarryingCapacity(speciesIndex);
            }

        }
    }
}
