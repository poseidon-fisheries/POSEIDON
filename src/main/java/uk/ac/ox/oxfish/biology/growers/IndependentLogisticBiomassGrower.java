package uk.ac.ox.oxfish.biology.growers;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Grows biomass in each given cell independently
 * Created by carrknight on 1/31/17.
 */
public class IndependentLogisticBiomassGrower implements Startable, Steppable{


    /**
     * list of biologies to grow. You can use a single grower for all the cells or a separate grower
     * for each cell. It shouldn't be too much of a big deal.
     */
    private List<BiomassLocalBiology> biologies = new LinkedList<>();



    /**
     * the uninpeded growth rate of each species
     */
    private final Double[] malthusianParameter;
    private Stoppable receipt;


    public IndependentLogisticBiomassGrower(Double[] malthusianParameter) {
        this.malthusianParameter = malthusianParameter;
    }

    @Override
    public void step(SimState simState) {

        FishState model = ((FishState) simState);

        //remove all the biologies that stopped
        biologies = biologies.stream().filter(
                logisticLocalBiology -> !logisticLocalBiology.isStopped()).collect(Collectors.toList());

        //for each place
        for(BiomassLocalBiology biology : biologies)
        {
            //grow fish

            Double[] currentBiomass = biology.getCurrentBiomass();
            assert (currentBiomass.length==malthusianParameter.length);

            for(int i=0; i<currentBiomass.length; i++)
            {
                assert currentBiomass[i] >=0;
                //grows logistically

                Double carryingCapacity = biology.getCarryingCapacity(i);
                if(carryingCapacity > FishStateUtilities.EPSILON && carryingCapacity > currentBiomass[i]) {
                    double oldBiomass = currentBiomass[i];
                    currentBiomass[i] = Math.min(carryingCapacity, currentBiomass[i] + malthusianParameter[i] *
                            (1d - currentBiomass[i] / carryingCapacity) * currentBiomass[i]);
                    //store recruitment number, counter should have been initialized by factory!
                    double recruitment = currentBiomass[i]-oldBiomass;
                    if(recruitment>FishStateUtilities.EPSILON)
                        model.getYearlyCounter().count(model.getSpecies().get(i) +
                                                               " Recruitment",
                                                       recruitment);

                }
                assert currentBiomass[i] >=0;
            }
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

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();

    }

    /**
     * Getter for property 'biologies'.
     *
     * @return Value for property 'biologies'.
     */
    public List<BiomassLocalBiology> getBiologies() {
        return biologies;
    }

    /**
     * Getter for property 'malthusianParameter'.
     *
     * @return Value for property 'malthusianParameter'.
     */
    public Double[] getMalthusianParameter() {
        return malthusianParameter;
    }
}
