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
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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

    public CommonLogisticGrower(double malthusianParameter, Species species) {
        this.malthusianParameter = malthusianParameter;
        this.species = species;
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
        //compute recruitment and distribute it at random
        recruitment = Math.min(recruitment,capacity-current);
        assert recruitment>=-FishStateUtilities.EPSILON;
        if(recruitment>FishStateUtilities.EPSILON) {
            //grow fish

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
}
