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

import static java.util.Arrays.stream;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.google.common.util.concurrent.AtomicDouble;
import ec.util.MersenneTwisterFast;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;

public abstract class FishBiomassAttractor
    extends AbstractFishAttractor<Double, BiomassLocalBiology, BiomassFad> {

    FishBiomassAttractor(
        final Collection<Species> species,
        final AttractionProbabilityFunction<BiomassLocalBiology, BiomassFad> attractionProbabilityFunction,
        final double[] attractionRates,
        final MersenneTwisterFast rng
    ) {
        super(species, attractionProbabilityFunction, attractionRates, rng);
    }

    @Override
    Double attractNothing(final Species species) {
        return 0.0;
    }

    @Override
    Entry<BiomassLocalBiology, Double> scale(
        final Map<Species, Entry<Double, Double>> attractedFish,
        final BiomassFad fad
    ) {
        final double[] biomassArray = new double[attractedFish.size()];
        final AtomicDouble totalBiomass = new AtomicDouble();
        attractedFish.forEach((species, catchAndWeight) -> {
            biomassArray[species.getIndex()] = catchAndWeight.getKey();
            totalBiomass.addAndGet(catchAndWeight.getValue());
        });
        return entry(
            new BiomassLocalBiology(scaleAttractedBiomass(biomassArray, fad)),
            totalBiomass.get()
        );
    }

    private double[] scaleAttractedBiomass(
        final double[] attractedBiomass,
        final BiomassFad fad
    ) {
        final double scalingFactor = biomassScalingFactor(
            stream(attractedBiomass).sum(),
            fad.getBiology().getTotalBiomass(),
            fad.getTotalCarryingCapacity()
        );
        return stream(attractedBiomass).map(b -> b * scalingFactor).toArray();
    }
}