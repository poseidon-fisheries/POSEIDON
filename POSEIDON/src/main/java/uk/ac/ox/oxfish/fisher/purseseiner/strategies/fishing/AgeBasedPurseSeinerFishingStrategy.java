/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2022-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractFadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.FadSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;

import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/**
 * like purse seiner fishing strategy, but decides whether to set on own fads depending on their
 * soak time (age) rather than the value of the fish underneath
 *
 * @param <B>
 */
public class AgeBasedPurseSeinerFishingStrategy<B extends LocalBiology>
    extends PurseSeinerFishingStrategy<B> {

    public AgeBasedPurseSeinerFishingStrategy(
        final Function<Fisher, Map<Class<? extends PurseSeinerAction>, Double>> actionWeightsLoader,
        final Function<Fisher, SetOpportunityDetector<B>> setOpportunityDetectorProvider,
        final Map<Class<? extends PurseSeinerAction>, DoubleUnaryOperator> actionValueFunctions,
        final ToDoubleFunction<Class<? extends PurseSeinerAction>> maxCurrentSpeeds,
        final double searchActionDecayConstant,
        final double fadDeploymentActionDecayConstant,
        final double movingThreshold
    ) {
        super(
            actionWeightsLoader,
            setOpportunityDetectorProvider,
            actionValueFunctions,
            maxCurrentSpeeds,
            searchActionDecayConstant,
            fadDeploymentActionDecayConstant,
            movingThreshold
        );
    }

    @Override
    protected double valueOfSetAction(
        final AbstractSetAction action,
        final Collection<Species> species
    ) {
        //if we know it is a fad action (own fad, not opportunistic)
        if (action instanceof FadSetAction) {
            assert ((AbstractFadSetAction) action).isOwnFad();
            final int stepItWasDeployed =
                ((AbstractFadSetAction) action).getFad().getStepDeployed();
            final int time = action.getFisher().grabState().getDay() - stepItWasDeployed;
            assert time >= 0;
            return time;
        } else {
            return super.valueOfSetAction(action, species);
        }
    }
}
