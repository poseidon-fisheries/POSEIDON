package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.Fishing;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.SeaTile;

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


    class Deploy implements PlannedAction{

        private final SeaTile tile;

        public Deploy(SeaTile tile) {
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

    class FadSet implements PlannedAction{


        private final Fad fadWePlanToSetOn;

        public FadSet(Fad fadWePlanToSetOn) {
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


    //very simple class, used to define the beginning and ending of a trip
    //in a plan: the action is always "arrival" at the end of the trip and "moving" at the beginning
    class Arrival implements PlannedAction{


        private final SeaTile position;

        private final boolean endOfTrip;

        public Arrival(SeaTile position, boolean endOfTrip) {
            this.position = position;
            this.endOfTrip = endOfTrip;
        }

        @Override
        public SeaTile getLocation() {
            return position;
        }

        @Override
        public double hoursItTake() {
            return 0;
        }

        /**
         * list of actions that need to take place for the planned action to take place
         *
         * @param fisher
         * @return
         */
        @Override
        public Action[] actuate(Fisher fisher) {
            return new Action[]{endOfTrip ? new Arriving() : new Moving()};
        }

        public boolean isEndOfTrip() {
            return endOfTrip;
        }
    }

    //this action represents an hour of fishing, followed by a # of hours of delay (due to local processing, recovery time,
    //or whatever else works)
    class Fishing implements PlannedAction{


        private final SeaTile position;

        private final double delayInHours;

        public Fishing(SeaTile position,
                       double delayInHours) {
            this.position = position;
            this.delayInHours = delayInHours;
        }

        @Override
        public SeaTile getLocation() {

            return position;
        }

        @Override
        public double hoursItTake() {
            return 1+delayInHours;
        }

        /**
         * list of actions that need to take place for the planned action to take place
         *
         * @param fisher
         * @return
         */
        @Override
        public Action[] actuate(Fisher fisher) {

            return delayInHours > 0 ?
                    new Action[]{new uk.ac.ox.oxfish.fisher.actions.Fishing(),new Delaying(delayInHours)} :
                    new Action[]{new uk.ac.ox.oxfish.fisher.actions.Fishing()};

        }


        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Fishing{");
            sb.append("position=").append(position);
            sb.append(", delayInHours=").append(delayInHours);
            sb.append('}');
            return sb.toString();
        }
    }

}
