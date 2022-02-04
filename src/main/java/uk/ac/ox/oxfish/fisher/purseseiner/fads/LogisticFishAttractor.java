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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

abstract class LogisticFishAttractor<WEIGHTED_CATCH extends Number, B extends LocalBiology, F extends Fad<B, F>>
        implements FishAttractor<B, F> {

    private final MersenneTwisterFast rng;
    private final double[] compressionExponents;
    private final double[] attractableBiomassCoefficients;
    private final double[] biomassInteractionCoefficients;
    private final double[] attractionRates;
    private final Species[] species;

    public LogisticFishAttractor(
            final MersenneTwisterFast rng,
            final Map<Species, Double> compressionExponents,
            final Map<Species, Double> attractableBiomassCoefficients,
            final Map<Species, Double> biomassInteractionCoefficients,
            final Map<Species, Double> attractionRates
    ) {
        this.rng = rng;
        // Store and arbitrary key set as our set of species
        this.species = attractionRates.keySet().toArray(new Species[0]);
        int maxIndex = Arrays.stream(this.species).mapToInt(value -> value.getIndex()).max().
                orElseThrow(() -> new RuntimeException("Fad Attractor with no species was provided!"));
        maxIndex++;
        this.attractionRates = new double[maxIndex];
        this.biomassInteractionCoefficients = new double[maxIndex];
        this.attractableBiomassCoefficients = new double[maxIndex];
        this.compressionExponents = new double[maxIndex];

        for (Species species : this.species) {
            this.attractionRates[species.getIndex()] = attractionRates.get(species);
            this.biomassInteractionCoefficients[species.getIndex()] = biomassInteractionCoefficients.get(species);
            this.attractableBiomassCoefficients[species.getIndex()] = attractableBiomassCoefficients.get(species);
            this.compressionExponents[species.getIndex()] = compressionExponents.get(species);
        }


    }

    @Override
    public WeightedObject<B> attract(
            final B seaTileBiology,
            final F fad
    ) {

        boolean attractedNothing = true;
        double fadBiomass = 0;
        for (Species currentSpecies : species) {
            fadBiomass+= fad.getBiology().getBiomass(currentSpecies);
        }

        HashMap<Species, WEIGHTED_CATCH> attractedFish = new HashMap<>(species.length);
        for (Species currentSpecies : species) {
            final double p =
                    probabilityOfAttraction(currentSpecies,
                            seaTileBiology.getBiomass(currentSpecies),
                            fadBiomass);
            if(p>0 && getRng().nextDouble() < p) {
                WEIGHTED_CATCH attracted = attractForSpecies(currentSpecies, seaTileBiology, fad);
                if(attracted.doubleValue()>0)
                    attractedNothing = false;
                attractedFish.put(currentSpecies,
                        attracted);
            } else {

                attractedFish.put(currentSpecies,
                        attractNothing(currentSpecies, fad));
            }

        }
        if(!attractedNothing)
            return scale(attractedFish, fad);
        else
            return null;
    }

    abstract WEIGHTED_CATCH attractForSpecies(Species s, B cellBiology, F fad);

    abstract WEIGHTED_CATCH attractNothing(Species s, F fad);

    double probabilityOfAttraction(
            final Species species,
            final double attractableBiomass,
            final double totalFadBiomass
    ) {
        return 1 - exp(-pow(
                attractableBiomassCoefficients[species.getIndex()] * attractableBiomass +
                        biomassInteractionCoefficients[species.getIndex()] * attractableBiomass
                                * totalFadBiomass
                , compressionExponents[species.getIndex()]
        ));
    }

    public MersenneTwisterFast getRng() {
        return rng;
    }

    abstract WeightedObject<B> scale(Map<Species, WEIGHTED_CATCH> attractedFish, F fad);

    public double getAttractionRates(Species species) {
        return attractionRates[species.getIndex()];
    }

}
