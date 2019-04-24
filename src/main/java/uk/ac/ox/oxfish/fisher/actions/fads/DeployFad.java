package uk.ac.ox.oxfish.fisher.actions.fads;

import static com.google.common.base.Preconditions.checkState;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;

import java.util.Optional;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

public class DeployFad implements FadAction {

    private final SeaTile seaTile;

    public DeployFad(SeaTile seaTile) { this.seaTile = seaTile; }

    @Override
    public boolean isPossible(FishState model, Fisher fisher) {
        return fisher.getLocation().getAltitude() <= 0 &&
            getFadManager(fisher).getNumFadsInStock() > 0;
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        checkState(seaTile == fisher.getLocation());
        checkState(isPossible(model, fisher));
        getFadManager(fisher).deployFad(seaTile);
        return new ActionResult(new Arriving(), hoursLeft - getDuration());
    }

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) { return Optional.of(seaTile); }

    @Override public double getDuration() {
        // TODO: what is the right number and where should it come from?
        return 1.0;
    }
}