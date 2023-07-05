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

package uk.ac.ox.oxfish.fisher.strategies.gear;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.equipment.gear.DecoratorGearPair;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.equipment.gear.GearDecorator;
import uk.ac.ox.oxfish.fisher.selfanalysis.CashFlowObjective;
import uk.ac.ox.oxfish.fisher.selfanalysis.DiscreteRandomAlgorithm;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.adaptation.ExploreImitateAdaptation;
import uk.ac.ox.oxfish.utility.adaptation.Sensor;
import uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing;
import uk.ac.ox.oxfish.utility.adaptation.maximization.RandomStep;
import uk.ac.ox.oxfish.utility.adaptation.probability.AdaptationProbability;

import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalInt;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static uk.ac.ox.oxfish.utility.adaptation.maximization.BeamHillClimbing.DEFAULT_DYNAMIC_NETWORK;

/**
 * Uses one gear at a time, updating it at scheduled times. Whenever it updates, it resets fishers' predictor
 * Created by carrknight on 6/13/16.
 */
public class PeriodicUpdateGearStrategy implements GearStrategy {


    final static public String tag = "PERIODIC_UPDATE_GEAR";
    @Nullable
    final List<Gear> options;
    /**
     * if true the gear is updated every year. Otherwise it is updated every month
     */
    final private boolean yearly;
    final private ExploreImitateAdaptation<? extends Gear> gearAdaptation;
    /**
     * this is always null except when it's time to change gear (this works as a flag to also reset predictors)
     */
    private Gear toReturn = null;
    /**
     * link to the fisher. Grabbed at start() only. Doubles as a flag of "being started"
     */
    private Fisher fisher;

    /**
     * given an exploration step, builds the adaptation algorithm around it
     *
     * @param yearly          whether to choose every year or every 2 months
     * @param explorationStep how an agent explores
     * @param probability     the probability of exploring, imitating and so on
     */
    public PeriodicUpdateGearStrategy(
        final boolean yearly,
        final RandomStep<Gear> explorationStep,
        final AdaptationProbability probability
    ) {
        options = null;
        this.yearly = yearly;
        this.gearAdaptation = new ExploreImitateAdaptation<>(
            fisher1 -> true,
            new BeamHillClimbing<>(true,
                true, DEFAULT_DYNAMIC_NETWORK,
                explorationStep
            ),
            (fisher, change, model) -> toReturn = change.makeCopy(),
            (Sensor<Fisher, Gear>) fisher -> fisher.getGear(),
            yearly ? new CashFlowObjective(365) : new CashFlowObjective(60),
            probability, a -> true
        );
    }


    /**
     * given a set of options, generates the adaptation algorithm around it
     *
     * @param yearly      whether to choose every year or every 2 months
     * @param options     the list of gear that is selectable
     * @param probability the probability of exploring, imitating and so on
     */
    public PeriodicUpdateGearStrategy(
        final boolean yearly,
        final List<Gear> options,
        final AdaptationProbability probability
    ) {
        this.options = options;
        this.yearly = yearly;
        this.gearAdaptation = new ExploreImitateAdaptation<>(
            fisher1 -> true,
            new DiscreteRandomAlgorithm<>(options),
            (fisher, change, model) -> {
                tagYourself(fisher, change, options);
                toReturn = change.makeCopy();
            },
            (Sensor<Fisher, Gear>) fisher -> fisher.getGear(),
            yearly ? new CashFlowObjective(365) : new CashFlowObjective(60),
            probability, a -> true,
            //copy only from others who have one of these gears!
            input -> input.getKey().getDirectedFriends().stream().filter(
                friend -> options.stream().anyMatch(gear -> friend.getGear().isSame(gear))
            ).collect(Collectors.toList())
        );
    }

    public void tagYourself(final Fisher fisher, final Gear change, final List<Gear> options) {
        final List<String> newTags = fisher.getTagsList()
            .stream()
            .filter(s -> !s.startsWith(tag))
            .collect(Collectors.toList());

        final OptionalInt indexOpt = IntStream.range(0, options.size())
            .filter(i ->
                change.isSame(options.get(i)))
            .findFirst();
        newTags.add(tag + "_" + indexOpt.orElse(-1));
        options.get(0).isSame(change);
        options.get(1).isSame(change);
        fisher.getTagsList().clear();
        fisher.getTagsList().addAll(newTags);
    }

    /**
     * choose gear to use for this trip
     *
     * @param fisher        the agent making a choice
     * @param random        the randomizer
     * @param model         the model
     * @param currentAction the action that triggered a call to this strategy
     * @return the gear to use. Null can be returned to mean: "use current gear"
     */
    @Override
    public void updateGear(
        final Fisher fisher,
        final MersenneTwisterFast random,
        final FishState model,
        final Action currentAction
    ) {
        if (toReturn != null) {
            Logger.getGlobal().fine(() -> fisher + " changing gear from " + fisher.getGear() + " to " + toReturn);
            fisher.resetDailyCatchesPredictors();

            //if it's a decorator, I am going to assume you want to replace it, as is
            if (toReturn instanceof GearDecorator) {
                assert !(((GearDecorator) toReturn).getDelegate() instanceof GearDecorator);
                //this assert might be wrong at some point but for now let's just assume
                // that if you swap a gear decorator out for another one you are
                // just targeting one level
                fisher.setGear(toReturn);
            } else {
                final DecoratorGearPair pair = DecoratorGearPair.getActualGear(fisher.getGear());
                //i am assuming you are swapping gears of the same type; if that's not true at some point
                //then delete this assert
                assert pair.getDecorated().getClass().equals(toReturn.getClass());
                if (pair.getDeepestDecorator() == null)
                    fisher.setGear(toReturn);
                else
                    pair.getDeepestDecorator().setDelegate(toReturn);
            }
            toReturn = null;
        }
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        this.fisher = fisher;

        if (options != null && !options.isEmpty())
            tagYourself(fisher, fisher.getGear(), options);


        //if started, adapt!
        if (yearly)
            fisher.addYearlyAdaptation(gearAdaptation);
        else
            fisher.addBiMonthlyAdaptation(gearAdaptation);

    }

    @Override
    public void turnOff(final Fisher fisher) {
        if (this.fisher != null) //if started, remov adaptations
        {
            if (yearly)
                this.fisher.removeYearlyAdaptation(gearAdaptation);
            else
                this.fisher.removeBiMonthlyAdaptation(gearAdaptation);
        }
    }

    public ExploreImitateAdaptation<? extends Gear> getGearAdaptation() {
        return gearAdaptation;
    }
}
