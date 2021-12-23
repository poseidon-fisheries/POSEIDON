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

import static java.lang.Math.min;
import static java.util.Comparator.comparingInt;
import static java.util.Map.Entry.comparingByKey;

import ec.util.MersenneTwisterFast;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;

public class LogisticFishBiomassAttractor
    extends LogisticFishAttractor<Double, BiomassLocalBiology, BiomassFad>
    implements FishBiomassAttractor {

    public LogisticFishBiomassAttractor(
        final MersenneTwisterFast rng,
        final Map<Species, Double> compressionExponents,
        final Map<Species, Double> attractableBiomassCoefficients,
        final Map<Species, Double> biomassInteractionCoefficients,
        final Map<Species, Double> attractionRates
    ) {
        super(
            rng,
            compressionExponents,
            attractableBiomassCoefficients,
            biomassInteractionCoefficients,
            attractionRates
        );
    }

    @Override
    Double attractForSpecies(
        final Species s, final BiomassLocalBiology cellBiology, final BiomassFad fad
    ) {
        final double fadBiomass = fad.getBiology().getBiomass(s);
        final double cellBiomass = cellBiology.getBiomass(s);
        final double attractionRate = getAttractionRates().get(s);
        final double fadCarryingCapacity = fad.getTotalCarryingCapacity();
        return min(
            cellBiomass,
            attractionRate *
                (1 + fadBiomass) *
                ((1 - fadBiomass) / fadCarryingCapacity)
        );
    }

    @Override
    Double attractNothing(final Species s, final BiomassFad fad) {
        return 0.0;
    }

    @Override
    BiomassLocalBiology scale(final Map<Species, Double> attractedFish, final BiomassFad fad) {
        final double[] biomassArray = attractedFish.entrySet().stream()
            .sorted(comparingByKey(comparingInt(Species::getIndex)))
            .mapToDouble(Entry::getValue)
            .toArray();
        return new BiomassLocalBiology(scaleAttractedBiomass(biomassArray, fad));
    }

}
