package uk.ac.ox.oxfish.fisher.strategies.fishing;

import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.oneOfFadsHere;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

import ec.util.MersenneTwisterFast;
import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.fads.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

public class RandomFadFishingStrategy implements FishingStrategy, FadManagerUtils {

    private final Collection<Function<Fisher, FadAction>> actions = new LinkedList<>();

    public RandomFadFishingStrategy() {
        actions.add(fisher -> new DeployFad(fisher.getLocation()));
        actions.add(fisher -> new MakeFadSet(oneOfFadsHere(fisher)
            .orElseThrow(() -> new RuntimeException("No FADs here!"))));
    }

    @NotNull
    private Stream<FadAction> possibleActions(FishState model, Fisher fisher) {
        return actions.stream()
            .map(f -> f.apply(fisher))
            .filter(action -> action.isPossible(model, fisher));
    }

    @Override
    public boolean shouldFish(
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {
        return currentTrip.getEffort() <= 0 &&
            possibleActions(model, fisher).findAny().isPresent();
    }

    @Override @NotNull
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        return oneOf(possibleActions(model, fisher), model.random)
            .map(action -> new ActionResult(action, hoursLeft))
            .orElse(new ActionResult(new Arriving(), 0));
    }

    @Override public void start(FishState model, Fisher fisher) { }

    @Override public void turnOff(Fisher fisher) { }

}
