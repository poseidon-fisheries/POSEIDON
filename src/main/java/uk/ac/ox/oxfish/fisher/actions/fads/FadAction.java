package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Optional;

public interface FadAction extends Action, FadManagerUtils {
    Optional<SeaTile> getActionTile(Fisher fisher);
    Quantity<Time> getDuration(Fisher fisher, MersenneTwisterFast rng);
    boolean isPossible(FishState model, Fisher fisher);

    default boolean isAllowed(FishState model, Fisher fisher) {
        return isAllowed(model, fisher, fisher.getLocation(), model.getStep());
    }

    default boolean isAllowed(FishState model, Fisher fisher, SeaTile actionTile, int actionStep) {
        return fisher.isCheater() || fisher.getRegulation().canFishHere(fisher, actionTile, model, actionStep);
    }

}
