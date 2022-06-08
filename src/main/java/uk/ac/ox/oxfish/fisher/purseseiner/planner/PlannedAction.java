package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import com.google.common.collect.ImmutableList;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.actions.FadSearchAction;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.LinkedList;
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
            return delayInHours <= 0 ?
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


    /**
     * stolen sets work a bit differently: you just show up at a place and steal
     * something through the FadSearchAction but if you don't find anything you
     * waste a bunch of time (also done through the FadSearchAction)
     */
    class OpportunisticFadSet implements PlannedAction{


        private final SeaTile whereAreWeGoingToSearchForFads;

        final private double hoursItTakesToSet;

        final private double hoursWastedIfNoFadAround;

        final private double minimumFadValueToSteal;

        public OpportunisticFadSet(SeaTile whereAreWeGoingToSearchForFads,
                                   double hoursItTakesToSet,
                                   double hoursWastedIfNoFadAround,
                                   double minimumFadValueToSteal) {
            this.whereAreWeGoingToSearchForFads = whereAreWeGoingToSearchForFads;
            this.hoursItTakesToSet = hoursItTakesToSet;
            this.hoursWastedIfNoFadAround = hoursWastedIfNoFadAround;
            this.minimumFadValueToSteal = minimumFadValueToSteal;
        }

        @Override
        public SeaTile getLocation() {

            return whereAreWeGoingToSearchForFads;
        }

        @Override
        public double hoursItTake() {
            //need to be pessimistic or you'll plan too many of these

            return hoursItTakesToSet + hoursWastedIfNoFadAround;
        }

        @Override
        public boolean isAllowedNow(Fisher fisher) {
            FadManager<? extends LocalBiology, ? extends Fad<?, ?>> fadManager = FadManager.getFadManager(fisher);
            return //you must be allowed at sea
                    fisher.isAllowedAtSea() &&

                            //fad setting ought not to be banned
                            !fadManager.getActionSpecificRegulations().isForbidden(OpportunisticFadSetAction.class,fisher) &&
                            //we should be allowed to fish here
                            fisher.isAllowedToFishHere(getLocation(),fisher.grabState())
                    ;
        }

        @Override
        public Action[] actuate(Fisher fisher) {
            return new Action[]{
                    new FadSearchAction(
                            hoursWastedIfNoFadAround,
                            hoursItTakesToSet,
                            minimumFadValueToSteal
                    )
            };
        }
    }



    //very simple class, used to define the beginning and ending of a trip
    //in a plan: the action is always "arrival" at the end of the trip and "moving" at the beginning
    static class Arrival implements PlannedAction{


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
    static class Fishing implements PlannedAction{


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


    //some of the purse seine gear stuff has "fit to data" catches per event
    //this is what we are using now
    static abstract class AbstractSetWithCatchSampler implements PlannedAction{

        private final static double DEFAULT_SET_DURATION = 2.69;

        private final CatchSampler howMuchWeCanFishOutGenerator;


        private final CatchMaker catchMaker;

        private final SeaTile position;

        private final double searchTimeInHours;

        private final double setDurationInHours;

        public AbstractSetWithCatchSampler(SeaTile position,
                                           CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                                           CatchMaker<? extends LocalBiology> catchMaker) {

            this(position,
                 howMuchWeCanFishOutGenerator,catchMaker ,
                 DEFAULT_SET_DURATION, 0d);
        }


        public AbstractSetWithCatchSampler(SeaTile position,
                                           CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                                           CatchMaker<? extends LocalBiology> catchMaker, double delayInHours) {
            this(position, howMuchWeCanFishOutGenerator,catchMaker ,
                 DEFAULT_SET_DURATION, delayInHours);
        }

        public AbstractSetWithCatchSampler(SeaTile position,
                                           CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                                           CatchMaker<? extends LocalBiology> catchMaker, double setDurationInHours,
                                           double delayInHours) {
            this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
            this.position = position;
            this.searchTimeInHours = delayInHours;
            this.setDurationInHours = setDurationInHours;
            this.catchMaker = catchMaker;
        }
        @Override
        public SeaTile getLocation() {

            return position;
        }

        abstract protected Class<? extends AbstractSetAction> getTypeOfActionPlanned();

        @Override
        public double hoursItTake() {
            return setDurationInHours+ searchTimeInHours;
        }

        @Override
        public boolean isAllowedNow(Fisher fisher) {

            FadManager<? extends LocalBiology, ? extends Fad<?, ?>> fadManager =
                    FadManager.getFadManager(fisher);
            return //you must be allowed at sea
                    fisher.isAllowedAtSea() &&
                            //fad setting ought not to be banned
                            !fadManager.getActionSpecificRegulations().
                                    isForbidden(getTypeOfActionPlanned(),fisher) &&
                            //we should be allowed to fish here
                            fisher.isAllowedToFishHere(getLocation(),fisher.grabState());

        }


        abstract protected <B extends LocalBiology>  AbstractSetAction<B>  createSet(
                B potentialCatch,
                Fisher fisher,
                double fishingTime,
                SeaTile location,
                CatchMaker catchMaker);

        /**
         * list of actions that need to take place for the planned action to take place
         *
         * @param fisher
         * @return
         */
        @Override
        public Action[] actuate(Fisher fisher) {


            LocalBiology potentialCatch =
                    (LocalBiology) howMuchWeCanFishOutGenerator.
                            apply(getLocation().getBiology());

            if(searchTimeInHours<=0)
            {
                return new Action[]{
                        createSet(potentialCatch, fisher, setDurationInHours,getLocation() ,catchMaker )
                };
            }
            else{
                return new Action[]{
                        createSet(potentialCatch, fisher, setDurationInHours,getLocation() ,catchMaker ),
                        new Delaying(searchTimeInHours)
                };
            }

        }


        @Override
        public String toString() {
            return "Planned set{" +
                    "position=" + position +
                    "type=" + getTypeOfActionPlanned() +
                    ", searchTimeInHours=" + searchTimeInHours +
                    ", setDurationInHours=" + setDurationInHours +
                    '}';
        }


    }

    //this is not the only way to do dolphin sets, in fact it is sort of improvised
    //but basically you decide you will go to a location and then you will draw
    //some catches and hope for the best
    static class DolphinSet extends  AbstractSetWithCatchSampler {


        public DolphinSet(SeaTile position, CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                          CatchMaker<? extends LocalBiology> catchMaker) {
            super(position, howMuchWeCanFishOutGenerator,catchMaker );
        }

        public DolphinSet(SeaTile position, CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                          CatchMaker<? extends LocalBiology> catchMaker, double delayInHours) {
            super(position, howMuchWeCanFishOutGenerator, catchMaker, delayInHours);
        }

        public DolphinSet(SeaTile position, CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                          CatchMaker<? extends LocalBiology> catchMaker, double setDurationInHours, double delayInHours) {
            super(position, howMuchWeCanFishOutGenerator,catchMaker , setDurationInHours, delayInHours);
        }

        @Override
        protected Class<? extends AbstractSetAction> getTypeOfActionPlanned() {
            return DolphinSetAction.class;
        }

        @Override
        protected <B extends LocalBiology> AbstractSetAction<B> createSet(
                B potentialCatch, Fisher fisher, double fishingTime, SeaTile location, CatchMaker catchMaker) {
            return new DolphinSetAction<>(
                    potentialCatch,
                    fisher,
                    fishingTime,
                    ImmutableList.of(location.getBiology()),
                    catchMaker
            );
        }
    }

    static class NonAssociatedSet extends  AbstractSetWithCatchSampler {


        final boolean canPoachFromFads;

        public NonAssociatedSet(SeaTile position, CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                                CatchMaker<? extends LocalBiology> catchMaker, double delayInHours) {
            this(position, howMuchWeCanFishOutGenerator, catchMaker, delayInHours,false);
        }

        public NonAssociatedSet(SeaTile position, CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
                                CatchMaker<? extends LocalBiology> catchMaker, double delayInHours,
                                boolean canPoachFromFads) {
            super(position, howMuchWeCanFishOutGenerator, catchMaker, delayInHours);
            this.canPoachFromFads = canPoachFromFads;
        }


        @Override
        protected Class<? extends AbstractSetAction> getTypeOfActionPlanned() {
            return NonAssociatedSetAction.class;
        }

        @Override
        protected <B extends LocalBiology> AbstractSetAction<B> createSet(
                B potentialCatch, Fisher fisher, double fishingTime, SeaTile location, CatchMaker catchMaker) {
            return new NonAssociatedSetAction<>(
                    potentialCatch,
                    fisher,
                    fishingTime,
                    canPoachFromFads ? getAllBiologiesHere(location,fisher) : ImmutableList.of(location.getBiology()),
                    catchMaker
            );
        }

        private static <B extends LocalBiology> List<B> getAllBiologiesHere(SeaTile tile, Fisher fisher){
            LinkedList<B> biologies = new LinkedList<>();
            biologies.add((B) tile.getBiology());
            final Bag fads = fisher.grabState().getFadMap().fadsAt(tile);
            for (Object fad : fads) {
                biologies.add((B) ((Fad) fad).getBiology());
            }
            return biologies;
        }
    }


}
