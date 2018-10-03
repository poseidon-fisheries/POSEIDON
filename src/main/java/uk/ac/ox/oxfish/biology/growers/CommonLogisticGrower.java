package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Preconditions;
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

    private final double distributionalWeight;

    public CommonLogisticGrower(double malthusianParameter, Species species, double distributionalWeight) {
        this.malthusianParameter = malthusianParameter;
        this.species = species;
        this.distributionalWeight = distributionalWeight;
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
            if(distributionalWeight>0)
                CommonLogisticGrower.allocateBiomassProportionally(biologies,
                                                                   recruitment,
                                                                   species.getIndex(),
                                                                   distributionalWeight);
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
     * @param biomassToAllocate biomass growth
     * @param speciesIndex the species number
     * @param distributionalWeight the higher, the more biomass will in proportion flow towards areas with higher carrying capacity
     */
    public static void allocateBiomassProportionally(
            List<? extends VariableBiomassBasedBiology> biologyList,
            double biomassToAllocate,
            int speciesIndex, double distributionalWeight){

        if(biomassToAllocate <FishStateUtilities.EPSILON) //don't bother with super tiny numbers
            return;
        double totalEmptySpace = 0;
        for (VariableBiomassBasedBiology local : biologyList) {
            totalEmptySpace += Math.pow(local.getCarryingCapacity(speciesIndex) - local.getCurrentBiomass()[speciesIndex],
                                        distributionalWeight);
        }

        if(!Double.isFinite(totalEmptySpace))
        {
            throw  new RuntimeException("Distributional weight too high, crashes the system");
        }

        /**
         * if you are using high distributional weights, some stuff might get full, so we need to recursively call this again
         */
        double leftOver = 0;

        //if there is less empty space than recruitment, just fill it all up!
        if(totalEmptySpace<= biomassToAllocate) {
            for (VariableBiomassBasedBiology local : biologyList) {
                local.getCurrentBiomass()[speciesIndex] = local.getCarryingCapacity(speciesIndex);
            }
        }
        else
        {
            //reallocate proportional to the empty space here compared to the total empty space
            for (VariableBiomassBasedBiology local : biologyList) {
                double emptySpace = Math.pow(local.getCarryingCapacity(speciesIndex) -
                                                     local.getCurrentBiomass()[speciesIndex],distributionalWeight);
                double addHere = biomassToAllocate * emptySpace / totalEmptySpace;
                if(local.getCurrentBiomass()[speciesIndex]+addHere > local.getCarryingCapacity(speciesIndex))
                {
                    leftOver += (addHere - (local.getCarryingCapacity(speciesIndex))-local.getCurrentBiomass()[speciesIndex]);
                    local.getCurrentBiomass()[speciesIndex] = local.getCarryingCapacity(speciesIndex);
                }
                else{
                    local.getCurrentBiomass()[speciesIndex] += addHere;

                }

                assert local.getCurrentBiomass()[speciesIndex] <= local.getCarryingCapacity(speciesIndex);
            }

        }

        assert leftOver>=0;
        assert leftOver< biomassToAllocate;
        if(leftOver>FishStateUtilities.EPSILON)
            allocateBiomassProportionally(biologyList,leftOver,speciesIndex,distributionalWeight);
    }
}
