package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.base.Preconditions;
import org.apache.commons.collections15.list.UnmodifiableList;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.data.IterativeAgerageBackAndForth;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings({"unchecked", "rawtypes"})
public class Plan {

    private final LinkedList<PlannedAction> plannedActions = new LinkedList<>();

    // a decorator to return for objects to look at the planned actions without being able to touch them
    private final List<PlannedAction> plannedActionsView = UnmodifiableList.decorate(plannedActions);

    private final IterativeAgerageBackAndForth<Integer>[] centroid = new IterativeAgerageBackAndForth[2];

    {
        centroid[0] = new IterativeAgerageBackAndForth();
        centroid[1] = new IterativeAgerageBackAndForth();
    }

    /**
     * every plan must have an initial path from starting to end position (which could be the same, as in most cycles)
     */
    public Plan(
        final SeaTile initialPosition,
        final SeaTile finalPosition
    ) {
        plannedActions.add(new PlannedAction.Arrival(initialPosition, false));
        plannedActions.add(new PlannedAction.Arrival(finalPosition, true));

        // update centroid of the path
        centroid[0].addObservationfromDouble(initialPosition.getGridX());
        centroid[0].addObservationfromDouble(finalPosition.getGridX());
        centroid[1].addObservationfromDouble(initialPosition.getGridY());
        centroid[1].addObservationfromDouble(finalPosition.getGridY());
    }

    public void insertAction(
        final PlannedAction newAction,
        final int indexInPathOfNewAction
    ) {
        Preconditions.checkArgument(
            indexInPathOfNewAction > 0,
            "You probably don't want to remove the very first step"
        );
        Preconditions.checkArgument(
            indexInPathOfNewAction <= plannedActions.size(),
            "You probably don't want to remove the very last step"
        );
        plannedActions.add(indexInPathOfNewAction, newAction);
        // update centroid
        centroid[0].addObservationfromDouble(newAction.getLocation().getGridX());
        centroid[1].addObservationfromDouble(newAction.getLocation().getGridY());
    }

    public int numberOfStepsInPath() {
        return plannedActions.size();
    }

    public PlannedAction peekNextAction() {
        return plannedActions.peekFirst();
    }

    public PlannedAction peekLastAction() {
        return plannedActions.peekLast();
    }

    public PlannedAction pollNextAction() {
        final PlannedAction toReturn = checkNotNull(plannedActions.poll());
        if (!plannedActions.isEmpty()) {
            if (toReturn.getLocation() != null) {
                // weird exception that can occur when you are targeting a moving object that has since left the map
                centroid[0].removeObservation(toReturn.getLocation().getGridX());
                centroid[1].removeObservation(toReturn.getLocation().getGridY());
            }
        } else {
            centroid[0] = new IterativeAgerageBackAndForth();
            centroid[1] = new IterativeAgerageBackAndForth();
        }
        return toReturn;
    }

    public double getGridXCentroid() {
        return centroid[0].getSmoothedObservation();
    }

    public double getGridYCentroid() {
        return centroid[1].getSmoothedObservation();
    }

    public List<PlannedAction> plannedActions() {
        return plannedActionsView;
    }

    @Override
    public String toString() {
        return "Plan{" + "plannedActions=" +
            plannedActions
                .stream().map(Object::toString)
                .collect(Collectors.joining("\n")) +
            '}';
    }
}
