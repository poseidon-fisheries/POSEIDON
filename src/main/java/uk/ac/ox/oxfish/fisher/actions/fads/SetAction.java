package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.PurseSeineGear;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Time;

/**
 * Represents either a FAD set or an unassociated set. Dolphin sets will presumable fall under this interface too.
 * It's not lost on me that making unassociated/dolphin sets extend FadAction is weird, so TODO: revise this.
 */
public interface SetAction extends FadAction {

    default Quantity<Time> getDuration(Fisher fisher, MersenneTwisterFast rng) {
        return ((PurseSeineGear) fisher.getGear()).nextSetDuration(rng);
    }

    default boolean isPossible(FishState model, Fisher fisher) {
        return fisher.getHold().getPercentageFilled() < 1;
    }

}
