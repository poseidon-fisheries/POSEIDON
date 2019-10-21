package uk.ac.ox.oxfish.fisher.actions.fads;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class DeployFad implements FadAction {

    private final SeaTile seaTile;

    public DeployFad(SeaTile seaTile) { this.seaTile = seaTile; }

    @Override
    public boolean isPossible(FishState model, Fisher fisher) {
        return fisher.getLocation().isWater() && getFadManager(fisher).getNumFadsInStock() > 0;
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        checkState(seaTile == fisher.getLocation());
        checkState(isPossible(model, fisher));
        getFadManager(fisher).deployFad(seaTile, model.random);
        return new ActionResult(new Arriving(), hoursLeft - toHours(getDuration()));
    }

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) { return Optional.of(seaTile); }

    @Override public Quantity<Time> getDuration() {
        // TODO: what is the right number and where should it come from?
        return getQuantity(1, HOUR);
    }
}
