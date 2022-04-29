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

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.gson.internal.$Gson$Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleUnaryOperator;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import sim.util.Bag;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.tuna.Aggregator;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.SchoolSetAction;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.SchoolSetActionMaker;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.utility.MasonUtils;

public class SchoolSetOpportunityGenerator<
    B extends LocalBiology,
    A extends SchoolSetAction<B>
    > extends SetOpportunityGenerator<B, A> {

    private final DoubleUnaryOperator probabilityFunction;
    private final ActiveOpportunities activeOpportunities;
    private final UnaryOperator<B> targetBiologyMaker;
    private final Aggregator<B> biologyAggregator;
    private final SchoolSetActionMaker<B, A> actionMaker;
    private final Map<Species, Double> weights;
    private final Class<B> biologyClass;

    public SchoolSetOpportunityGenerator(
        final DoubleUnaryOperator probabilityFunction,
        final Map<Species, Double> weights,
        final Class<B> biologyClass,
        final UnaryOperator<B> targetBiologyMaker,
        final SchoolSetActionMaker<B, A> actionMaker,
        final ActiveOpportunities activeOpportunities,
        final DoubleSupplier durationSampler,
        final Aggregator<B> biologyAggregator
    ) {
        super(durationSampler);
        this.probabilityFunction = probabilityFunction;
        this.activeOpportunities = activeOpportunities;
        this.biologyClass = biologyClass;
        this.targetBiologyMaker = targetBiologyMaker;
        this.biologyAggregator = biologyAggregator;

        // make sure all weights are >= 0 and at least one is > 0
        checkArgument(weights.values().stream().allMatch(w -> w >= 0));
        checkArgument(weights.values().stream().anyMatch(w -> w > 0));
        final double sum = weights.values().stream().mapToDouble(Double::doubleValue).sum();

        // normalize the weights so they sum up to one
        this.weights = weights.entrySet().stream()
            .collect(toImmutableMap(Entry::getKey, entry -> entry.getValue() / sum));

        this.actionMaker = actionMaker;
    }

    private List<B> biologiesHere(final Fisher fisher) {
        final SeaTile seaTile = fisher.getLocation();
        final Stream<B> tileBiology =
            Stream.of(seaTile.getBiology())
                .filter(biologyClass::isInstance)
                .map(biologyClass::cast);
        final Bag fads = fisher.grabState().getFadMap().fadsAt(seaTile);
        final Stream<B> fadBiologies = MasonUtils.<Fad<B, ?>>bagToStream(fads).map(Fad::getBiology);
        return Stream.concat(tileBiology, fadBiologies).collect(toImmutableList());
    }

    @Override
    public Collection<A> apply(final Fisher fisher) {
        final B biology = biologyAggregator.apply(
            fisher.grabState().getBiology(),
            biologiesHere(fisher)
        );
        final Int2D gridLocation = fisher.getLocation().getGridLocation();
        final int step = fisher.grabState().getStep();
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
        if (opportunity) {
            final B targetBiology = targetBiologyMaker.apply(biology);
            final double duration = getDurationSampler().getAsDouble();
            return ImmutableList.of(actionMaker.make(targetBiology, fisher, duration));
        } else {
            return ImmutableList.of();
        }
    }

    double probabilityOfOpportunity(final LocalBiology biology) {
        checkArgument(
            weights.keySet().stream().mapToDouble(biology::getBiomass).allMatch(b -> b >= 0)
        );
        final double weightedBiomass = weights
            .entrySet()
            .stream()
            .mapToDouble(entry -> biology.getBiomass(entry.getKey()) * entry.getValue())
            .sum();
        return probabilityFunction.applyAsDouble(weightedBiomass);
    }
}
