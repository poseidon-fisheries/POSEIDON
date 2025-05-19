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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;

import java.util.Collection;
import java.util.Map.Entry;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class LinearFishBiomassAttractor extends FishBiomassAttractor {

    public LinearFishBiomassAttractor(
        final Collection<Species> species,
        final AttractionProbabilityFunction attractionProbabilityFunction,
        final double[] attractionRates,
        final MersenneTwisterFast rng
    ) {
        super(species, attractionProbabilityFunction, attractionRates, rng);
    }

    @Override
    Entry<Double, Double> attractForSpecies(
        final Species species,
        final LocalBiology cellBiology,
        final BiomassAggregatingFad fad
    ) {
        final double caughtBiomass =
            getAttractionRate(species) * cellBiology.getBiomass(species);
        return entry(caughtBiomass, caughtBiomass);
    }
}
