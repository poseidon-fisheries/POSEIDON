/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;

import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;

/**
 * like purse seiner fishing strategy, but decides whether to set on own fads depending on their soak time (age) rather
 * than the value of the fish underneath
 * @param <B>
 * @param <F>
 */
public class AgeBasedPurseSeinerFishingStrategy<B extends LocalBiology, F extends Fad<B, F>>
        extends PurseSeinerFishingStrategy<B, F> {

    public AgeBasedPurseSeinerFishingStrategy(
            Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader,
            Function<Fisher, SetOpportunityDetector<B>> setOpportunityDetectorProvider,
            Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions,
            double searchActionDecayConstant, double fadDeploymentActionDecayConstant,
            double movingThreshold) {
        super(actionWeightsLoader, setOpportunityDetectorProvider, actionValueFunctions, searchActionDecayConstant,
              fadDeploymentActionDecayConstant, movingThreshold);
    }


    @Override
    protected double valueOfSetAction(
            @NotNull AbstractSetAction<? extends LocalBiology> action,
            DoubleUnaryOperator actionValueFunction, Collection<Species> species) {

        //if we know it is a fad action (own fad, not opportunistic)
        if(action instanceof FadSetAction)
        {
            assert ((FadSetAction<?, ?>) action).isOwnFad();
            int stepItWasDeployed = ((FadSetAction<?, ?>) action).getFad().getStepDeployed();
            int time = action.getFisher().grabState().getDay()-stepItWasDeployed;
            assert time>=0;
            if(time==0)
                return 0;
            return actionValueFunction.applyAsDouble(time);
        }
        else
            return super.valueOfSetAction(action, actionValueFunction, species);
    }
}
