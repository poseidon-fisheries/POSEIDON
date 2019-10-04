package uk.ac.ox.oxfish.fisher.strategies.fishing;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
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

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.Streams.stream;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils.oneOfFadsHere;
import static uk.ac.ox.oxfish.utility.MasonUtils.oneOf;

public class RandomFadFishingStrategy implements FishingStrategy, FadManagerUtils {

    private final ImmutableList<Function<Fisher, Optional<FadAction>>> actions =
        ImmutableList.of(
            fisher -> Optional.of(new DeployFad(fisher.getLocation())),
            fisher -> oneOfFadsHere(fisher).map(MakeFadSet::new)
        );

    @NotNull
    private Stream<FadAction> possibleActions(FishState model, Fisher fisher) {
        return actions.stream()
            .flatMap(f -> stream(f.apply(fisher)))
            .filter(action -> action.isPossible(model, fisher));
    }

    @Override
    public boolean shouldFish(
        Fisher fisher, MersenneTwisterFast random, FishState model, TripRecord currentTrip
    ) {
        return currentTrip.getEffort() <= 0 &&
            possibleActions(model, fisher).findAny().isPresent();
    }

    @Override
    @NotNull
    public ActionResult act(
        FishState model, Fisher fisher, Regulation regulation, double hoursLeft
    ) {
        final List<FadAction> possibleActions = possibleActions(model, fisher).collect(toList());
        return possibleActions.isEmpty() ?
            new ActionResult(new Arriving(), 0) :
            new ActionResult(oneOf(possibleActions, model.random), hoursLeft);
    }

    @Override
    public void start(FishState model, Fisher fisher) { }

    @Override
    public void turnOff(Fisher fisher) { }

}
