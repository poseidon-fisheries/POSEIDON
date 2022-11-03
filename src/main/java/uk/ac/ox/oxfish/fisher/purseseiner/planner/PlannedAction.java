package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * this represents either the next step in a plan or a potential next step in a plan.
 * It is described by where it should take place, how much it takes in time, and the type of action
 * that will take place once you are in position
 */
public interface PlannedAction {

    SeaTile getLocation();


    double hoursItTake();

    /**
     * list of actions that need to take place for the planned action to take place
     */
    Action[] actuate(Fisher fisher);

    /**
     * whether this action is allowed (by the regulations) were it to take place now
     *
     * @param fisher the fisher
     */
    //todo
    boolean isAllowedNow(Fisher fisher);


    class Deploy implements PlannedAction {

        private final SeaTile tile;

        private final double delayInHours;

        public Deploy(final SeaTile tile) {
            this(tile, 0);
        }

        public Deploy(final SeaTile tile, final double delayInHours) {
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
        public boolean isAllowedNow(final Fisher fisher) {
            return fisher.isAllowedAtSea() &&
                !FadManager.getFadManager(fisher)
                    .getActionSpecificRegulations()
                    .isForbidden(FadDeploymentAction.class, fisher);
        }

        @Override
        public Action[] actuate(final Fisher fisher) {
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

    class FadSet implements PlannedAction {


        private final AbstractFad fadWePlanToSetOn;

        public FadSet(final AbstractFad fadWePlanToSetOn) {
            this.fadWePlanToSetOn = fadWePlanToSetOn;
        }

        public static boolean isFadSetAllowed(
            final Fisher fisher,
            final FadManager<? extends LocalBiology, ? extends AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>> fadManager,
            final AbstractFad set
        ) {
            return fisher.isAllowedAtSea() &&
                //the fad has not since been destroyed
                !set.isLost() &&
                //fad setting ought not to be banned
                !fadManager.getActionSpecificRegulations().isForbidden(FadSetAction.class, fisher) &&
                //we should be allowed to fish here
                fisher.isAllowedToFishHere(set.getLocation(), fisher.grabState());
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
        public boolean isAllowedNow(final Fisher fisher) {
            final FadManager<? extends LocalBiology, ? extends AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>> fadManager = FadManager
                .getFadManager(fisher);
            return isFadSetAllowed(fisher, fadManager, fadWePlanToSetOn);
        }

        @Override
        public Action[] actuate(final Fisher fisher) {
            //noinspection unchecked
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
    class OpportunisticFadSet implements PlannedAction {


        private final SeaTile whereAreWeGoingToSearchForFads;

        final private double hoursItTakesToSet;

        final private double hoursWastedIfNoFadAround;

        final private double minimumFadValueToSteal;

        public OpportunisticFadSet(
            final SeaTile whereAreWeGoingToSearchForFads,
            final double hoursItTakesToSet,
            final double hoursWastedIfNoFadAround,
            final double minimumFadValueToSteal
        ) {
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
        public boolean isAllowedNow(final Fisher fisher) {
            final FadManager<? extends LocalBiology, ? extends AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>> fadManager = FadManager
                .getFadManager(fisher);
            return //you must be allowed at sea
                fisher.isAllowedAtSea() &&

                    //fad setting ought not to be banned
                    !fadManager.getActionSpecificRegulations().isForbidden(OpportunisticFadSetAction.class, fisher) &&
                    //we should be allowed to fish here
                    fisher.isAllowedToFishHere(getLocation(), fisher.grabState())
                ;
        }

        @Override
        public Action[] actuate(final Fisher fisher) {
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
    class Arrival implements PlannedAction {

        private final SeaTile position;

        private final boolean endOfTrip;

        public Arrival(final SeaTile position, final boolean endOfTrip) {
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
        public boolean isAllowedNow(final Fisher fisher) {
            return fisher.isAllowedAtSea();
        }

        /**
         * list of actions that need to take place for the planned action to take place
         */
        @Override
        public Action[] actuate(final Fisher fisher) {
            return new Action[]{endOfTrip ? new Arriving() : new Moving()};
        }

        public boolean isEndOfTrip() {
            return endOfTrip;
        }
    }

    //this action represents an hour of fishing, followed by a # of hours of delay (due to local processing, recovery time,
    //or whatever else works)
    class Fishing implements PlannedAction {


        private final SeaTile position;

        private final double delayInHours;

        public Fishing(
            final SeaTile position,
            final double delayInHours
        ) {
            this.position = position;
            this.delayInHours = delayInHours;
        }

        @Override
        public SeaTile getLocation() {
            return position;
        }

        @Override
        public double hoursItTake() {
            return 1 + delayInHours;
        }

        @Override
        public boolean isAllowedNow(final Fisher fisher) {
            return fisher.isAllowedAtSea() && fisher.isAllowedToFishHere(position, fisher.grabState());
        }

        /**
         * list of actions that need to take place for the planned action to take place
         */
        @Override
        public Action[] actuate(final Fisher fisher) {

            return delayInHours > 0 ?
                new Action[]{new uk.ac.ox.oxfish.fisher.actions.Fishing(), new Delaying(delayInHours)} :
                new Action[]{new uk.ac.ox.oxfish.fisher.actions.Fishing()};

        }

        @Override
        public String toString() {
            return "Fishing{" + "position=" + position +
                ", delayInHours=" + delayInHours +
                '}';
        }
    }

    //some of the purse seine gear stuff has "fit to data" catches per event
    //this is what we are using now
    abstract class AbstractSetWithCatchSampler implements PlannedAction {

        protected final static double DEFAULT_SET_DURATION = 2.69;

        private final CatchSampler howMuchWeCanFishOutGenerator;

        private final CatchMaker catchMaker;

        private final SeaTile position;

        private final double searchTimeInHours;

        private final double setDurationInHours;

        private final TargetBiologiesGrabber targetBiologiesGrabber;

        public AbstractSetWithCatchSampler(
            final SeaTile position,
            final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker
        ) {

            this(position,
                howMuchWeCanFishOutGenerator, catchMaker,
                DEFAULT_SET_DURATION, 0d, false, -1
            );
        }

        public AbstractSetWithCatchSampler(
            final SeaTile position,
            final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker, final double delayInHours
        ) {
            this(position, howMuchWeCanFishOutGenerator, catchMaker,
                DEFAULT_SET_DURATION, delayInHours, false, -1
            );
        }

        public AbstractSetWithCatchSampler(
            final SeaTile position,
            final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker, final double setDurationInHours,
            final double delayInHours, final boolean canPoachFromFads, final int rangeInSeaTiles
        ) {
            this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
            this.position = position;
            this.searchTimeInHours = delayInHours;
            this.setDurationInHours = setDurationInHours;
            this.catchMaker = catchMaker;
            this.targetBiologiesGrabber = new TargetBiologiesGrabber(canPoachFromFads, rangeInSeaTiles);
        }

        @Override
        public SeaTile getLocation() {
            return position;
        }

        public TargetBiologiesGrabber getTargetBiologiesGrabber() {
            return targetBiologiesGrabber;
        }

        abstract protected Class<? extends AbstractSetAction> getTypeOfActionPlanned();

        @Override
        public double hoursItTake() {
            return setDurationInHours + searchTimeInHours;
        }

        @Override
        public boolean isAllowedNow(final Fisher fisher) {

            final FadManager<? extends LocalBiology, ? extends AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>> fadManager =
                FadManager.getFadManager(fisher);
            return //you must be allowed at sea
                fisher.isAllowedAtSea() &&
                    //fad setting ought not to be banned
                    !fadManager.getActionSpecificRegulations().
                        isForbidden(getTypeOfActionPlanned(), fisher) &&
                    //we should be allowed to fish here
                    fisher.isAllowedToFishHere(getLocation(), fisher.grabState());

        }

        abstract protected <B extends LocalBiology> AbstractSetAction<B> createSet(
            B potentialCatch,
            Fisher fisher,
            double fishingTime,
            SeaTile location,
            CatchMaker catchMaker
        );

        /**
         * list of actions that need to take place for the planned action to take place
         */
        @Override
        public Action[] actuate(final Fisher fisher) {


            if (searchTimeInHours <= 0) {
                return new Action[]{
                    new PotentialSetAction(this, fisher)
                };
            } else {
                return new Action[]{
                    new PotentialSetAction(this, fisher),
                    new Delaying(searchTimeInHours)
                };
            }

        }

        private Action turnToAction(final Fisher fisher) {
            @SuppressWarnings("unchecked") final LocalBiology potentialCatch =
                (LocalBiology) howMuchWeCanFishOutGenerator.
                    apply(getTargetBiologiesGrabber().getLocalBiologiesAndAggregateThem(getLocation(), fisher));
            return createSet(potentialCatch, fisher, setDurationInHours, getLocation(), catchMaker);
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

    /**
     * an object that prepares itself to use a catch sampler to fish stuff out but
     * has not sampled yet (avoiding spoiling the sampler for sets that may end up not happening)
     */
    class PotentialSetAction implements Action {

        private final PlannedAction.AbstractSetWithCatchSampler generator;

        private final Fisher fisher;

        public PotentialSetAction(final AbstractSetWithCatchSampler generator, final Fisher fisher) {
            this.generator = generator;
            this.fisher = fisher;
        }

        @Override
        public ActionResult act(
            final FishState model,
            final Fisher agent,
            final Regulation regulation,
            final double hoursLeft
        ) {
            return generator.turnToAction(fisher).act(model, agent, regulation, hoursLeft);
        }
    }

    //this is not the only way to do dolphin sets, in fact it is sort of improvised
    //but basically you decide you will go to a location and then you will draw
    //some catches and hope for the best
    class DolphinSet extends AbstractSetWithCatchSampler {

        public DolphinSet(
            final SeaTile position, final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker
        ) {
            super(position, howMuchWeCanFishOutGenerator, catchMaker);
        }

        public DolphinSet(
            final SeaTile position, final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker, final double delayInHours
        ) {
            super(position, howMuchWeCanFishOutGenerator, catchMaker, delayInHours);
        }

        public DolphinSet(
            final SeaTile position,
            final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker,
            final double setDurationInHours,
            final double delayInHours
        ) {
            super(position, howMuchWeCanFishOutGenerator, catchMaker, setDurationInHours, delayInHours,
                false, -1
            );
        }

        public DolphinSet(
            final SeaTile position,
            final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker,
            final double delayInHours,
            final boolean canPoachFromFads,
            final int rangeInSeatiles
        ) {
            super(position, howMuchWeCanFishOutGenerator, catchMaker, DEFAULT_SET_DURATION, delayInHours,
                canPoachFromFads, rangeInSeatiles
            );
        }

        @Override
        protected Class<? extends AbstractSetAction> getTypeOfActionPlanned() {
            return DolphinSetAction.class;
        }

        @Override
        protected <B extends LocalBiology> AbstractSetAction<B> createSet(
            final B potentialCatch,
            final Fisher fisher,
            final double fishingTime,
            final SeaTile location,
            final CatchMaker catchMaker
        ) {
            return new DolphinSetAction<>(
                potentialCatch,
                fisher,
                fishingTime,
                getTargetBiologiesGrabber().buildListOfCatchableAreas(getLocation(), fisher),
                catchMaker
            );
        }
    }

    class NonAssociatedSet extends AbstractSetWithCatchSampler {

        public NonAssociatedSet(
            final SeaTile position, final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker, final double delayInHours
        ) {
            this(position, howMuchWeCanFishOutGenerator, catchMaker, delayInHours, false, -1);
        }

        public NonAssociatedSet(
            final SeaTile position, final CatchSampler<? extends LocalBiology> howMuchWeCanFishOutGenerator,
            final CatchMaker<? extends LocalBiology> catchMaker, final double delayInHours,
            final boolean canPoachFromFads,
            final int rangeInSeaTiles
        ) {
            super(
                position,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                DEFAULT_SET_DURATION,
                delayInHours,
                canPoachFromFads,
                rangeInSeaTiles
            );
        }

        @Override
        protected Class<? extends AbstractSetAction> getTypeOfActionPlanned() {
            return NonAssociatedSetAction.class;
        }

        @Override
        protected <B extends LocalBiology> AbstractSetAction<B> createSet(
            final B potentialCatch,
            final Fisher fisher,
            final double fishingTime,
            final SeaTile location,
            final CatchMaker catchMaker
        ) {
            return new NonAssociatedSetAction<>(
                potentialCatch,
                fisher,
                fishingTime,
                getTargetBiologiesGrabber().buildListOfCatchableAreas(getLocation(), fisher),
                catchMaker
            );
        }
    }
}
