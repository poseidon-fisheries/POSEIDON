package uk.ac.ox.oxfish.biology.initializer;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.BiomassDiffuser;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.growers.LogisticGrowerInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

/**
 *
 * The logistic local biologies now daily share their biomass with their poorer neighbors
 * Created by carrknight on 6/22/15.
 */
public class DiffusingLogisticInitializer extends IndependentLogisticInitializer
{

    /**
     * fixes a limit on how much biomass can leave the sea-tile
     */
    private final double percentageLimitOnDailyMovement;

    /**
     * how much of the differential between two seatile's biomass should be solved by movement in a single day
     */
    private final double differentialPercentageToMove;


    public DiffusingLogisticInitializer(
            DoubleParameter carryingCapacity,
            DoubleParameter minInitialCapacity, DoubleParameter maxInitialCapacity,
            double percentageLimitOnDailyMovement,
            double differentialPercentageToMove,
            LogisticGrowerInitializer grower) {
        super(carryingCapacity, minInitialCapacity, maxInitialCapacity,grower);
        this.percentageLimitOnDailyMovement = percentageLimitOnDailyMovement;
        this.differentialPercentageToMove = differentialPercentageToMove;
    }

    /**
     * Call the independent logistic initializer but add a steppable to call to smooth fish around
     *  @param biology the global biology instance
     * @param map     the map which by now should have all the tiles in place
     * @param random  mersenne randomizer
     * @param model
     */
    @Override
    public void processMap(
            GlobalBiology biology, NauticalMap map, MersenneTwisterFast random, FishState model) {
        super.processMap(biology, map, random, model);

        BiomassDiffuser diffuser = new BiomassDiffuser(map,random,biology,
                                                       differentialPercentageToMove,
                                                       percentageLimitOnDailyMovement);
        model.scheduleEveryDay(diffuser,StepOrder.BIOLOGY_PHASE);


    }


}
