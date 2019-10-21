package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManagerUtils;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

public abstract class PlanBasedFadDestinationStrategy implements DestinationStrategy, FadManagerUtils {

    final Queue<FadAction> actionQueue = new LinkedList<>();
    final NauticalMap map;

    PlanBasedFadDestinationStrategy(NauticalMap map) { this.map = map; }

    /**
     * Returns the next reachable destination tile from the action queue, removing actions without a
     * valid destination. When the action queue is empty, the fisher goes back to port.
     */
    private SeaTile destinationTile(Fisher fisher) {
        return actionQueue.isEmpty() ?
            fisher.getHomePort().getLocation() : // go back to port if nothing planned
            actionQueue.element().getActionTile(fisher) // look for next destination otherwise
                .orElseGet(() -> { // if the queued action didn't result in a valid destination...
                    actionQueue.remove(); // ...remove it from the queue...
                    return destinationTile(fisher); // ...and look for the next one.
                });
    }

    abstract void makeNewPlan(Fisher fisher);

    @NotNull
    public Optional<FadAction> pollActionQueue() {
        return Optional.ofNullable(actionQueue.poll());
    }

    public boolean isActionQueueEmpty() { return actionQueue.isEmpty(); }

    @Override
    public SeaTile chooseDestination(
        Fisher fisher, MersenneTwisterFast random, FishState model, Action currentAction
    ) {
        if (fisher.isGoingToPort() && !actionQueue.isEmpty()) actionQueue.clear();
        if (fisher.isAtPort() && actionQueue.isEmpty()) makeNewPlan(fisher);
        return destinationTile(fisher);
    }

}
