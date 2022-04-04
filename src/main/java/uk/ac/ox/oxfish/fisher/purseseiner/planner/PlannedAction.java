package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.List;

/**
 * this represents either the next step in a plan or a potential next step in a plan.
 * It is described by where it should take place, how much it takes in time, and the type of action
 * that will take place once you are in position
 */
@SuppressWarnings("ALL")
public interface PlannedAction {

    public SeaTile getLocation();


    public double hoursItTake();

    /**
     * list of actions that need to take place for the planned action to take place
     * @return
     */
    public Action[] actuate(Fisher fisher);

    /**
     * whether this action will be allowed at the time we plan for it to take place
     * @param fisher the fisher
     * @param modelTimeStep the step it will take place
     */
    //todo
   // public boolean isAllowedAtStep(Fisher fisher, int modelTimeStep);


    class PlannedDeploy implements PlannedAction{

        private final SeaTile tile;

        public PlannedDeploy(SeaTile tile) {
            this.tile = tile;
        }

        @Override
        public SeaTile getLocation() {
            return tile;
        }

        @Override
        public double hoursItTake() {
            return 0;
        }

        @Override
        public Action[] actuate(Fisher fisher){
            return new Action[]{new FadDeploymentAction(fisher)};
        }

        @Override
        public String toString() {
            return "PlannedDeploy{" +
                    "tile=" + tile +
                    '}';
        }
    }

    class PlannedFadSet implements PlannedAction{


        private final Fad fadWePlanToSetOn;

        public PlannedFadSet(Fad fadWePlanToSetOn) {
            this.fadWePlanToSetOn = fadWePlanToSetOn;
        }

        @Override
        public SeaTile getLocation() {
            return fadWePlanToSetOn.getLocation();
        }

        @Override
        public double hoursItTake() {
            return 1;
        }

        @Override
        public Action[] actuate(Fisher fisher) {
            return new Action[]{
                    new FadSetAction(
                            fadWePlanToSetOn,
                            fisher,
                            hoursItTake()
                    )
            };
        }
    }
}
