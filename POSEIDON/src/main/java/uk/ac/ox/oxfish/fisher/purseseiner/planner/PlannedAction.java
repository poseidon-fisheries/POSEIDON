/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.fisher.purseseiner.planner;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.*;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.samplers.CatchSampler;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.List;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.*;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

/**
 * this represents either the next step in a plan or a potential next step in a plan. It is described by where it should
 * take place, how much it takes in time, and the type of action that will take place once you are in position
 */
public interface PlannedAction {

    static boolean isActionAllowed(
        final Fisher fisher,
        final SeaTile location,
        final uk.ac.ox.poseidon.agents.api.Action action
    ) {
        return fisher.isAllowedAtSea() &&
            fisher.grabState().getRegulations().isPermitted(action) &&
            // we should be allowed to fish here
            fisher.isAllowedToFishHere(location, fisher.grabState());
    }

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
    // todo
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    boolean isAllowedNow(Fisher fisher);

    ActionType getActionType();

    class Deploy implements PlannedAction {

        private final SeaTile tile;

        private final double delayInHours;

        public Deploy(final SeaTile tile) {
            this(tile, 0);
        }

        public Deploy(
            final SeaTile tile,
            final double delayInHours
        ) {
            this.tile = tile;
            this.delayInHours = delayInHours;
        }

        @Override
        public double hoursItTake() {
            return delayInHours; // the deployment itself is immediate
        }

        @Override
        public Action[] actuate(final Fisher fisher) {
            return delayInHours <= 0
                ? new Action[]{new FadDeploymentAction(fisher)}
                : new Action[]{new FadDeploymentAction(fisher), new Delaying(delayInHours)};
        }

        @Override
        public boolean isAllowedNow(final Fisher fisher) {
            return fisher.isAllowedAtSea() &&
                !fisher.grabState()
                    .getRegulations()
                    .isForbidden(new FadDeploymentAction(fisher, tile));
        }

        @Override
        public SeaTile getLocation() {
            return tile;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.DeploymentAction;
        }

        @Override
        public String toString() {
            return "PlannedDeploy{" +
                "tile=" + tile +
                '}';
        }
    }

    class FadSet implements PlannedAction {

        private final Fad fadWePlanToSetOn;

        public FadSet(final Fad fadWePlanToSetOn) {
            this.fadWePlanToSetOn = fadWePlanToSetOn;
        }

        @Override
        public SeaTile getLocation() {
            return fadWePlanToSetOn.isLost() ? null : fadWePlanToSetOn.getLocation();
        }

        @Override
        public boolean isAllowedNow(final Fisher fisher) {
            return isFadSetAllowed(fisher, getFadManager(fisher), fadWePlanToSetOn);
        }

        public static boolean isFadSetAllowed(
            final Fisher fisher,
            final FadManager fadManager,
            final Fad fad
        ) {
            return !fad.isLost() && // the fad has not since been destroyed
                isActionAllowed(
                    fisher,
                    fad.getLocation(),
                    new FadSetAction(fad, fisher, 0) // fake action just to check for legality
                );
        }

        @Override
        public ActionType getActionType() {
            return ActionType.SetOwnFadAction;
        }

        @Override
        public Action[] actuate(final Fisher fisher) {
            return new Action[]{
                new FadSetAction(
                    fadWePlanToSetOn,
                    fisher,
                    hoursItTake()
                )
            };
        }

        @Override
        public double hoursItTake() {
            return 1;
        }
    }

    /**
     * stolen sets work a bit differently: you just show up at a place and steal something through the FadSearchAction
     * but if you don't find anything you waste a bunch of time (also done through the FadSearchAction)
     */
    class OpportunisticFadSet implements PlannedAction {

        private final SeaTile whereAreWeGoingToSearchForFads;

        final private double hoursItTakesToSet;

        final private double hoursWastedIfNoFadAround;

        final private double minimumFadValueToSteal;

        final private double probabilityOfFindingOtherFads;

        public OpportunisticFadSet(
            final SeaTile whereAreWeGoingToSearchForFads,
            final double hoursItTakesToSet,
            final double hoursWastedIfNoFadAround,
            final double minimumFadValueToSteal,
            final double probabilityOfFindingOtherFads
        ) {
            this.whereAreWeGoingToSearchForFads = whereAreWeGoingToSearchForFads;
            this.hoursItTakesToSet = hoursItTakesToSet;
            this.hoursWastedIfNoFadAround = hoursWastedIfNoFadAround;
            this.minimumFadValueToSteal = minimumFadValueToSteal;
            this.probabilityOfFindingOtherFads = probabilityOfFindingOtherFads;
        }

        @Override
        public double hoursItTake() {
            // need to be pessimistic or you'll plan too many of these
            return hoursItTakesToSet + hoursWastedIfNoFadAround;
        }

        @Override
        public boolean isAllowedNow(final Fisher fisher) {
            return isActionAllowed(
                fisher,
                getLocation(),
                new FadManager.DummyAction( // fake action just to check for legality
                    OFS.name(),
                    fisher,
                    fisher.grabState().getMap().getCoordinates(getLocation())
                )
            );
        }

        @Override
        public SeaTile getLocation() {
            return whereAreWeGoingToSearchForFads;
        }

        @Override
        public ActionType getActionType() {
            return ActionType.OpportunisticFadSets;
        }

        @Override
        public Action[] actuate(final Fisher fisher) {
            return new Action[]{
                new FadSearchAction(
                    hoursWastedIfNoFadAround,
                    hoursItTakesToSet,
                    minimumFadValueToSteal,
                    probabilityOfFindingOtherFads
                )
            };
        }
    }

    // very simple class, used to define the beginning and ending of a trip
    // in a plan: the action is always "arrival" at the end of the trip and "moving" at the beginning
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    class Arrival implements PlannedAction {

        private final SeaTile position;

        private final boolean endOfTrip;

        public Arrival(
            final SeaTile position,
            final boolean endOfTrip
        ) {
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

        @Override
        public ActionType getActionType() {
            return null; // we don't have an action type for "Arrival"
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

    // this action represents an hour of fishing, followed by a # of hours of delay (due to local processing, recovery
    // time,
    // or whatever else works)
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

        @Override
        public ActionType getActionType() {
            return ActionType.FishingOnTile;
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

    // some of the purse seine gear stuff has "fit to data" catches per event
    // this is what we are using now
    abstract class AbstractSetWithCatchSampler<B extends LocalBiology>
        implements PlannedAction {

        protected final static double DEFAULT_SET_DURATION = 2.69;

        private final CatchSampler<B> howMuchWeCanFishOutGenerator;

        private final CatchMaker<B> catchMaker;

        private final SeaTile position;

        private final double searchTimeInHours;

        private final double setDurationInHours;

        private final TargetBiologiesGrabber<B> targetBiologiesGrabber;

        public AbstractSetWithCatchSampler(
            final SeaTile position,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final double setDurationInHours,
            final double delayInHours,
            final boolean canPoachFromFads,
            final int rangeInSeaTiles,
            final Class<B> localBiologyClass
        ) {
            this.howMuchWeCanFishOutGenerator = howMuchWeCanFishOutGenerator;
            this.position = position;
            this.searchTimeInHours = delayInHours;
            this.setDurationInHours = setDurationInHours;
            this.catchMaker = catchMaker;
            this.targetBiologiesGrabber = new TargetBiologiesGrabber<>(
                canPoachFromFads,
                rangeInSeaTiles,
                localBiologyClass
            );
        }

        @Override
        public double hoursItTake() {
            return setDurationInHours + searchTimeInHours;
        }

        @Override
        public boolean isAllowedNow(final Fisher fisher) {
            return isActionAllowed(
                fisher,
                getLocation(),
                makeDummyPlannedAction(fisher)
            );
        }

        @Override
        public SeaTile getLocation() {
            return position;
        }

        private uk.ac.ox.poseidon.agents.api.Action makeDummyPlannedAction(final Fisher fisher) {
            return new FadManager.DummyAction(
                getActionCode(),
                fisher,
                fisher.grabState().getMap().getCoordinates(getLocation())
            );
        }

        abstract String getActionCode();

        /**
         * list of actions that need to take place for the planned action to take place
         */
        @Override
        public Action[] actuate(final Fisher fisher) {
            if (searchTimeInHours <= 0) {
                return new Action[]{
                    new PotentialSetAction<>(this, fisher)
                };
            } else {
                return new Action[]{
                    new PotentialSetAction<>(this, fisher),
                    new Delaying(searchTimeInHours)
                };
            }

        }

        private Action turnToAction(final Fisher fisher) {
            final Entry<List<B>, Supplier<B>> entry =
                getTargetBiologiesGrabber().grabTargetBiologiesAndAggregator(getLocation(), fisher);
            final B potentialCatch = howMuchWeCanFishOutGenerator.apply(entry.getValue().get());
            final List<B> targetBiologies = entry.getKey();
            return createSet(potentialCatch, targetBiologies, fisher, setDurationInHours, getLocation(), catchMaker);
        }

        public TargetBiologiesGrabber<B> getTargetBiologiesGrabber() {
            return targetBiologiesGrabber;
        }

        abstract protected AbstractSetAction createSet(
            final B potentialCatch,
            final List<B> targetBiologies,
            final Fisher fisher,
            final double fishingTime,
            final SeaTile location,
            final CatchMaker<B> catchMaker
        );

        @Override
        public String toString() {
            return "Planned set{" +
                "position=" + position +
                "type=" + this.getClass() +
                ", searchTimeInHours=" + searchTimeInHours +
                ", setDurationInHours=" + setDurationInHours +
                '}';
        }
    }

    /**
     * an object that prepares itself to use a catch sampler to fish stuff out but has not sampled yet (avoiding
     * spoiling the sampler for sets that may end up not happening)
     */
    class PotentialSetAction<B extends LocalBiology> implements Action {

        private final PlannedAction.AbstractSetWithCatchSampler<B> generator;

        private final Fisher fisher;

        public PotentialSetAction(
            final AbstractSetWithCatchSampler<B> generator,
            final Fisher fisher
        ) {
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

    // this is not the only way to do dolphin sets, in fact it is sort of improvised
    // but basically you decide you will go to a location and then you will draw
    // some catches and hope for the best
    class DolphinSet<B extends LocalBiology>
        extends AbstractSetWithCatchSampler<B> {

        public DolphinSet(
            final SeaTile position,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final double delayInHours,
            final boolean canPoachFromFads,
            final int rangeInSeatiles,
            final Class<B> localBiologyClass
        ) {
            super(
                position,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                DEFAULT_SET_DURATION,
                delayInHours,
                canPoachFromFads,
                rangeInSeatiles,
                localBiologyClass
            );
        }

        @Override
        String getActionCode() {
            return DEL.name();
        }

        @Override
        protected AbstractSetAction createSet(
            final B potentialCatch,
            final List<B> targetBiologies,
            final Fisher fisher,
            final double fishingTime,
            final SeaTile location,
            final CatchMaker<B> catchMaker
        ) {
            return new DolphinSetAction<>(
                potentialCatch,
                fisher,
                fishingTime,
                targetBiologies,
                catchMaker
            );
        }

        @Override
        public ActionType getActionType() {
            return ActionType.DolphinSets;
        }
    }

    class NonAssociatedSet<B extends LocalBiology>
        extends AbstractSetWithCatchSampler<B> {

        public NonAssociatedSet(
            final SeaTile position,
            final CatchSampler<B> howMuchWeCanFishOutGenerator,
            final CatchMaker<B> catchMaker,
            final double delayInHours,
            final boolean canPoachFromFads,
            final int rangeInSeaTiles,
            final Class<B> localBiologyClass
        ) {
            super(
                position,
                howMuchWeCanFishOutGenerator,
                catchMaker,
                DEFAULT_SET_DURATION,
                delayInHours,
                canPoachFromFads,
                rangeInSeaTiles,
                localBiologyClass
            );
        }

        @Override
        String getActionCode() {
            return NOA.name();
        }

        @Override
        protected AbstractSetAction createSet(
            final B potentialCatch,
            final List<B> targetBiologies,
            final Fisher fisher,
            final double fishingTime,
            final SeaTile location,
            final CatchMaker<B> catchMaker
        ) {
            return new NonAssociatedSetAction<>(
                potentialCatch,
                fisher,
                fishingTime,
                targetBiologies,
                catchMaker
            );
        }

        @Override
        public ActionType getActionType() {
            return ActionType.NonAssociatedSets;
        }
    }
}
