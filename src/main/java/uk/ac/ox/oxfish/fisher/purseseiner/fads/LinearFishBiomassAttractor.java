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
import static java.util.Comparator.comparingInt;
import static java.util.Map.Entry.comparingByKey;

import com.google.common.collect.ImmutableMap;

import java.util.DoubleSummaryStatistics;
import java.util.Map;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.Species;

public class LinearFishBiomassAttractor implements FishBiomassAttractor {

    private final Map<Species, Double> attractionRates;

    public LinearFishBiomassAttractor(
        final Map<Species, Double> attractionRates
    ) {
        this.attractionRates = ImmutableMap.copyOf(attractionRates);
    }

    @Override
    public WeightedObject<BiomassLocalBiology> attract(
        final BiomassLocalBiology seaTileBiology,
        final BiomassFad fad
    ) {
        DoubleSummaryStatistics totalAttraction = new DoubleSummaryStatistics();
        final double[] attractedBiomass = attractionRates.entrySet()
            .stream()
            .sorted(comparingByKey(comparingInt(Species::getIndex)))
            .mapToDouble(entry -> {
                final Species species = entry.getKey();
                final Double attractionRate = entry.getValue();
                double attracted = attractionRate * seaTileBiology.getBiomass(species);
                totalAttraction.accept(attracted);
                return attracted;
            })
            .toArray();

        return new WeightedObject<>(
                new BiomassLocalBiology(scaleAttractedBiomass(attractedBiomass, fad)),
                totalAttraction.getSum()
        );
    }

}
