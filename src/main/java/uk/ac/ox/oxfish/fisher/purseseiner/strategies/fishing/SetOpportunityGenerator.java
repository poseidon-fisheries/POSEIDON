/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

public class SetOpportunityGenerator {

    private final LogisticFunction probabilityFunction;
    private final ActiveOpportunities activeOpportunities;
    private final Function<Fisher, AbstractSetAction> actionConstructor;
    private final Map<Species, Double> weights;

    public SetOpportunityGenerator(
        final double logisticMidpoint,
        final double logisticSteepness,
        final Map<Species, Double> weights,
        final Function<Fisher, AbstractSetAction> actionConstructor,
        final ActiveOpportunities activeOpportunities
    ) {
        this(
            new LogisticFunction(logisticMidpoint, logisticSteepness),
            weights,
            actionConstructor,
            activeOpportunities
        );
    }

    private SetOpportunityGenerator(
        final LogisticFunction probabilityFunction,
        final Map<Species, Double> weights,
        final Function<Fisher, AbstractSetAction> actionConstructor,
        final ActiveOpportunities activeOpportunities
    ) {
        this.probabilityFunction = probabilityFunction;
        this.activeOpportunities = activeOpportunities;

        // make sure all weights are >= 0 and at least one is > 0
        checkArgument(weights.values().stream().allMatch(w -> w >= 0));
        checkArgument(weights.values().stream().anyMatch(w -> w > 0));
        final double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();

        // normalize the weights so they sum up to one
        this.weights = weights.entrySet().stream()
            .collect(toImmutableMap(Entry::getKey, entry -> entry.getValue() / sum));

        this.actionConstructor = actionConstructor;
    }

    Optional<AbstractSetAction> get(
        final Fisher fisher,
        final LocalBiology biology,
        final Int2D gridLocation,
        final int step
    ) {
        final boolean opportunity;
        if (activeOpportunities.hasOpportunity(gridLocation, step)) {
            opportunity = true;
        } else {
            final double p = probabilityOfOpportunity(biology);
            opportunity = fisher.grabRandomizer().nextBoolean(p);
            if (opportunity) {
                final int duration = 1;
                activeOpportunities.addOpportunity(gridLocation, step, duration);
            }
        }
        return opportunity
            ? Optional.of(actionConstructor.apply(fisher))
            : Optional.empty();
    }

    double probabilityOfOpportunity(final LocalBiology biology) {
        checkArgument(weights.keySet().stream().mapToDouble(biology::getBiomass).allMatch(b -> b >= 0));
        final double weightedBiomass = weights
            .entrySet()
            .stream()
            .mapToDouble(entry -> biology.getBiomass(entry.getKey()) * entry.getValue())
            .sum();
        return probabilityFunction.applyAsDouble(weightedBiomass);
    }

}
