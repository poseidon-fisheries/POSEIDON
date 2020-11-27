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

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.utils.LogisticFunction;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class SetOpportunityGenerator {

    private final LogisticFunction probabilityFunction;
    private final Function<Fisher, AbstractSetAction> actionConstructor;
    private final Map<Species, Double> weights;

    public SetOpportunityGenerator(
        final double logisticMidpoint,
        final double logisticSteepness,
        final Map<Species, Double> weights,
        final Function<Fisher, AbstractSetAction> actionConstructor
    ) {
        this.probabilityFunction = new LogisticFunction(logisticMidpoint, logisticSteepness);
        this.weights = ImmutableMap.copyOf(weights);
        this.actionConstructor = actionConstructor;
    }

    Optional<AbstractSetAction> get(Fisher fisher, final LocalBiology biology) {
        final double p = probabilityOfOpportunity(biology);
        return fisher.grabRandomizer().nextBoolean(p)
            ? Optional.of(actionConstructor.apply(fisher))
            : Optional.empty();
    }

    private double probabilityOfOpportunity(final LocalBiology biology) {
        final double weightedBiomass = weights
            .entrySet()
            .stream()
            .mapToDouble(entry -> biology.getBiomass(entry.getKey()) * entry.getValue())
            .sum();
        return probabilityFunction.applyAsDouble(weightedBiomass);
    }

}
