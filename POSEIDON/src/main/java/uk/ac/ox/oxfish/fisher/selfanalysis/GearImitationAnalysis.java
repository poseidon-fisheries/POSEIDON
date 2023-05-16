/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.selfanalysis;

import ec.util.MersenneTwisterFast;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.DockingListener;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.FisherStartable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Logger;

/**
 * Run every two months, check how are you doing, then check a friend. If he is doing better than you have a small probability
 * of copying their gear (without paying the price)
 * Created by carrknight on 8/4/15.
 */
public class GearImitationAnalysis {

    public static final Actuator<Fisher, Gear> DEFAULT_GEAR_ACTUATOR = new Actuator<Fisher, Gear>() {
        @Override
        public void apply(final Fisher fisher1, final Gear change, final FishState model) {
            Logger.getGlobal().fine(() -> fisher1 + " is about to change gear");
            //predictions are wrong: reset at the end of the trip
            fisher1.addDockingListener(new DockingListener() {
                boolean active = true;

                @Override
                public void dockingEvent(final Fisher fisher, final Port port) {
                    if (!active)
                        return;
                    fisher1.setGear(change.makeCopy());
                    Logger.getGlobal().fine(() -> fisher1 + " has changed gear and will reset its predictor");
                    fisher1.resetDailyCatchesPredictors();
                    active = false;
                    final DockingListener outer = this;
                    //schedule to remove the listener
                    model.scheduleOnce((Steppable) simState -> fisher1.removeDockingListener(outer), StepOrder.DAWN);


                }
            });


        }
    };


    /**
     * creates a bimonthly adaptation to increase or decrease the size of the hold available for each fisher
     *
     * @param fishers a list of fisher to adapt
     * @param model   the fishstate
     */
    public static void attachHoldSizeAnalysisToEachFisher(
        final List<Fisher> fishers, final FishState model
    ) {

        final int species = model.getBiology().getSize();

        //add analysis
        for (final Fisher fisher : fishers) {

            final ExploreImitateAdaptation<Hold> holdAdaptation = new ExploreImitateAdaptation<>(
                fisher1 -> true,
                new BeamHillClimbing<Hold>(
                    new RandomStep<Hold>() {
                        public Hold randomStep(
                            final FishState state,
                            final MersenneTwisterFast random,
                            final Fisher fisher,
                            final Hold current
                        ) {
                            return new Hold(
                                fisher.getMaximumHold() *
                                    (.8 + .4 * random.nextDouble()),
                                state.getBiology()
                            );
                        }
                    }

                ), (fisher1, change, model1) -> fisher1.changeHold(change),
                fisher1 -> {
                    //create a new hold for scanning. Helps with safety plus we can't get Fisher hold
                    return new Hold(fisher1.getMaximumHold(), model.getBiology());
                }, new CashFlowObjective(60), .15, .6, new Predicate<Hold>() {
                @Override
                public boolean test(final Hold a) {
                    return true;
                }
            }
            );


            model.registerStartable(new FisherStartable() {
                @Override
                public void start(final FishState model, final Fisher fisher) {
                    fisher.addBiMonthlyAdaptation(holdAdaptation);
                }

                @Override
                public void turnOff(final Fisher fisher) {
                    fisher.removeBiMonthlyAdaptation(holdAdaptation);
                }
            }, fisher);
        }


        model.getDailyDataSet().registerGatherer("Holding Size", state -> {
            final double size = state.getFishers().size();
            if (size == 0)
                return Double.NaN;
            else {
                double total = 0;
                for (final Fisher fisher1 : state.getFishers())
                    total += fisher1.getMaximumHold();
                return total / size;
            }
        }, Double.NaN);


    }


    public static void attachGoingOutProbabilityToEveryone(
        final List<Fisher> fishers,
        final FishState model, final double shockSize, final double explorationProbability,
        final double imitationProbability
    ) {
        for (final Fisher fisher : fishers) {
            final ExploreImitateAdaptation<FixedProbabilityDepartingStrategy> departingChance
                = new ExploreImitateAdaptation<>(
                fisher1 -> true,
                new BeamHillClimbing<FixedProbabilityDepartingStrategy>(
                    new RandomStep<FixedProbabilityDepartingStrategy>() {
                        @Override
                        public FixedProbabilityDepartingStrategy randomStep(
                            final FishState state, final MersenneTwisterFast random, final Fisher fisher,
                            final FixedProbabilityDepartingStrategy current
                        ) {
                            double probability = current.getProbabilityToLeavePort();
                            final double shock = (2 * shockSize) * random.nextDouble() - shockSize;
                            probability = probability * (1 + shock);
                            probability = Math.min(Math.max(0, probability), 1);
                            return new FixedProbabilityDepartingStrategy(probability, false);
                        }
                    }
                ),
                (fisher1, change, model1) -> fisher1.setDepartingStrategy(change),
                fisher1 -> ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()),
                new CashFlowObjective(60),
                explorationProbability, imitationProbability, new Predicate<FixedProbabilityDepartingStrategy>() {
                @Override
                public boolean test(final FixedProbabilityDepartingStrategy a) {
                    return true;
                }
            }
            );
            fisher.addBiMonthlyAdaptation(departingChance);


        }
        model.getDailyDataSet().registerGatherer("Probability to leave port", state1 -> {
            final double size = state1.getFishers().size();
            if (size == 0)
                return Double.NaN;
            else {
                double total = 0;
                for (final Fisher fisher1 : state1.getFishers())
                    total += ((FixedProbabilityDepartingStrategy) fisher1.getDepartingStrategy()).
                        getProbabilityToLeavePort();
                return total / size;
            }
        }, Double.NaN);


    }


}
