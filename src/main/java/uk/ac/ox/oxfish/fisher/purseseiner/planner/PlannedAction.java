package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadDeploymentAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.regs.fads.YearlyActionLimitRegulation;

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
     * whether this action is allowed (by the regulations) were it to take place now
     * @param fisher the fisher
     * @param modelTimeStep the step it will take place
     */
    //todo
    public boolean isAllowedNow(Fisher fisher);


    class Deploy implements PlannedAction{

        private final SeaTile tile;

        private final double delayInHours;

        public Deploy(SeaTile tile) {
            this(tile,0);
        }

        public Deploy(SeaTile tile, double delayInHours) {
            this.tile = tile;
            this.delayInHours = delayInHours;
        }

        @Override
        public SeaTile getLocation() {
            return tile;
        }

        @Override
        public double hoursItTake() {
            return delayInHours; //the deployment itself is immediate
        }

        @Override
        public boolean isAllowedNow(Fisher fisher) {
            return fisher.isAllowedAtSea() &&
                    !FadManager.getFadManager(fisher).getActionSpecificRegulations().isForbidden(FadDeploymentAction.class,fisher);
        }

        @Override
        public Action[] actuate(Fisher fisher){
            return delayInHours > 0 ?
                    new Action[]{new FadDeploymentAction(fisher)} :
                    new Action[]{new FadDeploymentAction(fisher), new Delaying(delayInHours)}
                    ;
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
            return fadWePlanToSetOn.isLost() ? null : fadWePlanToSetOn.getLocation();
        }

        @Override
        public double hoursItTake() {
            return 1;
        }

        @Override
        public boolean isAllowedNow(Fisher fisher) {
            FadManager<? extends LocalBiology, ? extends Fad<?, ?>> fadManager = FadManager.getFadManager(fisher);
            return //you must be allowed at sea
                    fisher.isAllowedAtSea() &&
                            //the fad has not since been destroyed
                            !fadWePlanToSetOn.isLost() &&
                            //fad setting ought not to be banned
                            !fadManager.getActionSpecificRegulations().isForbidden(FadSetAction.class,fisher) &&
                            //we should be allowed to fish here
                            fisher.isAllowedToFishHere(getLocation(),fisher.grabState())
                    ;
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

        @Override
        public boolean isAllowedNow(Fisher fisher) {
            return fisher.isAllowedAtSea();
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

        @Override
        public boolean isAllowedNow(Fisher fisher) {
            return fisher.isAllowedAtSea() && fisher.isAllowedToFishHere(position,fisher.grabState());
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
