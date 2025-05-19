/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.function.UnaryOperator.identity;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public abstract class AbstractFishAttractor<
    A,
    B extends LocalBiology,
    F extends AggregatingFad<B, F>>
    implements FishAttractor<B, F> {

    private final double[] attractionRates;
    private final List<Species> species;
    private final AttractionProbabilityFunction attractionProbabilityFunction;
    private final MersenneTwisterFast rng;

    AbstractFishAttractor(
        final Collection<Species> species,
        final AttractionProbabilityFunction attractionProbabilityFunction,
        final double[] attractionRates,
        final MersenneTwisterFast rng
    ) {
        this.species = ImmutableList.copyOf(species);
        this.attractionProbabilityFunction = attractionProbabilityFunction;
        this.attractionRates = attractionRates.clone();
        this.rng = rng;
    }

    @Override
    public WeightedObject<B> attractImplementation(
        final LocalBiology seaTileBiology,
        final F fad
    ) {
        final Set<Species> attractedSpecies = attractedSpecies(seaTileBiology, fad);
        if (attractedSpecies.isEmpty()) {
            return null;
        }

        final Map<Species, Entry<A, Double>> attractedFish =
            species.stream().collect(toImmutableMap(
                identity(),
                s -> attractedSpecies.contains(s)
                    ? attractForSpecies(s, seaTileBiology, fad)
                    : entry(attractNothing(s), 0.0)
            ));

        return attractedFish.values().stream().anyMatch(n -> n.getValue() > 0)
            ? WeightedObject.from(scale(attractedFish, fad))
            : null;
    }

    private Set<Species> attractedSpecies(
        final LocalBiology seaTileBiology,
        final AggregatingFad<?, ?> fad
    ) {
        return species
            .stream()
            .filter(species ->
                rng.nextBoolean(
                    attractionProbabilityFunction.apply(
                        species,
                        seaTileBiology,
                        fad
                    )
                )
            ).collect(toImmutableSet());
    }

    abstract Entry<A, Double> attractForSpecies(Species species, LocalBiology cellBiology, F fad);

    abstract A attractNothing(Species species);

    abstract Entry<B, Double> scale(Map<Species, Entry<A, Double>> attractedFish, F fad);

    double getAttractionRate(final Species species) {
        return attractionRates[species.getIndex()];
    }

}
