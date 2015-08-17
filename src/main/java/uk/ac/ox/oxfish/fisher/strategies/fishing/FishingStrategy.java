package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.FisherEquipment;
import uk.ac.ox.oxfish.fisher.FisherMemory;
import uk.ac.ox.oxfish.fisher.FisherStatus;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.Startable;

/**
 * The strategy that decides whether or not to fish once arrived and keeps being queried
 * Created by carrknight on 4/22/15.
 */
public interface FishingStrategy extends FisherStartable{

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param equipment
     * @param status
     *@param memory
     * @param random the randomizer
     * @param model the model itself   @return true if the fisher should fish here, false otherwise
     */
    boolean shouldFish(
            FisherEquipment equipment, FisherStatus status, FisherMemory memory, MersenneTwisterFast random,
            FishState model);



}
