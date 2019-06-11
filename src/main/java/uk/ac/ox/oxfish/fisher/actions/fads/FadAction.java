package uk.ac.ox.oxfish.fisher.actions.fads;

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
    Quantity<Time> getDuration();
    boolean isPossible(FishState model, Fisher fisher);
}
