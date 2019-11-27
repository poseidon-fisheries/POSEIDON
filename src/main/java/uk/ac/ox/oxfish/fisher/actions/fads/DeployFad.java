package uk.ac.ox.oxfish.fisher.actions.fads;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultipleRegulations;
import uk.ac.ox.oxfish.model.regs.NoFishing;
import uk.ac.ox.oxfish.model.regs.Regulation;
import uk.ac.ox.oxfish.model.regs.TemporaryRegulation;
import uk.ac.ox.oxfish.model.regs.fads.IATTC;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkState;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.getFadManager;
import static uk.ac.ox.oxfish.utility.Measures.toHours;

public class DeployFad implements FadAction {

    // TODO: that should probably be configurable, but there is no good place to put it...
    private static final int BUFFER_PERIOD_BEFORE_CLOSURE = 15;

    public static final String NUMBER_OF_FAD_DEPLOYMENTS = "Total number of FAD deployments";

    private final SeaTile seaTile;

    public DeployFad(SeaTile seaTile) { this.seaTile = seaTile; }

    /**
     * This little piece of ugliness is my "solution" to the problem of disallowing FAD deployments 15 days before
     * the start of a temporary closure. It recursively digs down the regulation hierarchy to see if a NoFishing
     * regulation will be active at the specified step. It currently assumes that the regulation is some combination
     * of MultipleRegulations and TemporaryRegulation (meaning it wouldn't work with, e.g., ArbitraryPause).
     * The proper way to handle something like this would be to build the concept of "action specific regulations"
     * into the whole regulation system, but I fear that would cross the line from refactoring to re-architecturing.
     */
    private boolean isNoFishingAtStep(final Regulation regulation, FishState model, int step) {
        if (regulation instanceof NoFishing)
            return true;
        else if (regulation instanceof TemporaryRegulation)
            return isNoFishingAtStep(((TemporaryRegulation) regulation).delegateAtStep(model, step), model, step);
        else if (regulation instanceof MultipleRegulations)
            return ((MultipleRegulations) regulation)
                .getRegulations().stream()
                .anyMatch(r -> isNoFishingAtStep(r, model, step));
        else
            return false;
    }

    /**
     * Deploying a FAD is allowed if we can fish and if there is no closure kicking in within the buffer period.
     */
    @Override public boolean isAllowed(FishState model, Fisher fisher, SeaTile actionTile, int actionStep) {

        final Regulation regulation = fisher.getRegulation();
        return regulation.canFishHere(fisher, actionTile, model, actionStep) &&
            !isNoFishingAtStep(regulation, model, actionStep + BUFFER_PERIOD_BEFORE_CLOSURE);
    }

    @Override
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        checkState(seaTile == fisher.getLocation());
        if (isAllowed(model, fisher) && isPossible(model, fisher)) {
            getFadManager(fisher).deployFad(seaTile, model.getStep(), model.random);
            fisher.getYearlyCounter().count(NUMBER_OF_FAD_DEPLOYMENTS, 1);
        }
        return new ActionResult(new Arriving(), hoursLeft - toHours(getDuration()));
    }

    @Override
    public boolean isPossible(FishState model, Fisher fisher) {
        final FadManager fadManager = getFadManager(fisher);
        return fisher.getLocation().isWater() &&
            fadManager.getNumDeployedFads() < IATTC.activeFadsLimit(fisher) &&
            fadManager.getNumFadsInStock() > 0;
    }

    @Override public Quantity<Time> getDuration() {
        // see https://github.com/poseidon-fisheries/tuna/issues/6
        return getQuantity(0, HOUR);
    }

    @Override
    public Optional<SeaTile> getActionTile(Fisher fisher) { return Optional.of(seaTile); }
}
