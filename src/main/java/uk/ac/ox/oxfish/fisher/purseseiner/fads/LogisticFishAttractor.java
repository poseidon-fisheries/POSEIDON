/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Math.exp;
import static java.lang.Math.pow;
import static java.util.function.UnaryOperator.identity;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import java.util.Map;
import java.util.Set;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

abstract class LogisticFishAttractor<A, B extends LocalBiology, F extends Fad<B, F>>
    implements FishAttractor<B, F> {

    private final MersenneTwisterFast rng;
    private final Map<Species, Double> compressionExponents;
    private final Map<Species, Double> attractableBiomassCoefficients;
    private final Map<Species, Double> biomassInteractionCoefficients;
    private final Map<Species, Double> attractionRates;
    private final Set<Species> species;

    public LogisticFishAttractor(
        final MersenneTwisterFast rng,
        final Map<Species, Double> compressionExponents,
        final Map<Species, Double> attractableBiomassCoefficients,
        final Map<Species, Double> biomassInteractionCoefficients,
        final Map<Species, Double> attractionRates
    ) {
        this.rng = rng;
        this.compressionExponents = ImmutableMap.copyOf(compressionExponents);
        this.attractableBiomassCoefficients = ImmutableMap.copyOf(attractableBiomassCoefficients);
        this.biomassInteractionCoefficients = ImmutableMap.copyOf(biomassInteractionCoefficients);
        this.attractionRates = ImmutableMap.copyOf(attractionRates);

        // Store and arbitrary key set as our set of species
        this.species = this.attractionRates.keySet();
    }

    @Override
    public B attract(
        final B seaTileBiology,
        final F fad
    ) {
        final double fadBiomass = fad.getBiology().getTotalBiomass(species);
        final Map<Species, A> attractedFish = species
            .stream()
            .collect(toImmutableMap(identity(), s -> {
                final double p =
                    probabilityOfAttraction(s, seaTileBiology.getBiomass(s), fadBiomass);
                return getRng().nextDouble() < p
                    ? attractForSpecies(s, seaTileBiology, fad)
                    : attractNothing(s, fad);
            }));

        return scale(attractedFish, fad);
    }

    abstract A attractForSpecies(Species s, B cellBiology, F fad);

    abstract A attractNothing(Species s, F fad);

    private double probabilityOfAttraction(
        final Species species,
        final double attractableBiomass,
        final double totalFadBiomass
    ) {
        return 1 - pow(exp(
            attractableBiomassCoefficients.get(species) * attractableBiomass +
                biomassInteractionCoefficients.get(species) * attractableBiomass
                    * totalFadBiomass
        ), compressionExponents.get(species));
    }

    public MersenneTwisterFast getRng() {
        return rng;
    }

    abstract B scale(Map<Species, A> attractedFish, F fad);

    Map<Species, Double> getAttractionRates() {
        return attractionRates;
    }

}
