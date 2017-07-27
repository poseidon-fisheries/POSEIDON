package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.QuotaPerSpecieRegulation;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.List;

/**
 * A fishing return decorator that would stop fishing whenever the fish in hold is higher the quota owned
 * Created by carrknight on 7/27/17.
 */
public class QuotaLimitDecorator implements FishingStrategy {


    private final FishingStrategy decorated;


    public QuotaLimitDecorator(FishingStrategy decorated) {
        this.decorated = decorated;
    }

    @Override
    public void start(FishState model, Fisher fisher) {
        decorated.start(model,fisher);
    }

    @Override
    public void turnOff(Fisher fisher) {
        decorated.turnOff(fisher);
    }

    /**
     * This is called by the fisher to decide whether or not to fish and then each step after that to decide whether or
     * not to continue fishing
     *
     * @param fisher
     * @param random      the randomizer
     * @param model       the model itself
     * @param currentTrip
     * @return true if the fisher should fish here, false otherwise
     */
    @Override
    public boolean shouldFish(
            Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip) {
        return decorated.shouldFish(fisher,random,model,currentTrip)
                &&
                check(fisher.getRegulation(),fisher.getHold(),model.getSpecies());
    }



    private boolean check(Regulation regulation, Hold hold, List<Species> speciesList){

        if(regulation instanceof QuotaPerSpecieRegulation)
        {
            for(Species species : speciesList)
            {
                //if it's a protected species for which you have quotas
                double quotaHeld = ((QuotaPerSpecieRegulation) regulation).getQuotaRemaining(species.getIndex());
                if(quotaHeld > 0
                    && //and you already hold more than you can sell
                  quotaHeld *1.1 < hold.getWeightOfCatchInHold(species))
                    //then stop fishing!
                    return false;

            }
        }
        return true;

    }
}
