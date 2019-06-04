package uk.ac.ox.oxfish.fisher.actions.fads;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import javax.measure.quantity.Time;

public interface FadAction extends Action, FadManagerUtils {
    Optional<SeaTile> getActionTile(Fisher fisher);
    Time getDuration();
    boolean isPossible(FishState model, Fisher fisher);
}
