/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.HashMap;

/**
 * takes a snapshot of the current distribution of biomass and stores it as an array to allocate biomass when asked again
 */
public class SnapshotBiomassAllocator implements BiomassAllocator {

    private HashMap<SeaTile, Double> weightMap = null;


    public void takeSnapshort(
        NauticalMap map,
        Species species
    ) {
        weightMap = new HashMap<>();
        double total = 0;
        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            if (!seaTile.isFishingEvenPossibleHere())
                continue;
            Double biomassHere = seaTile.getBiomass(species);
            Preconditions.checkState(Double.isFinite(biomassHere));
            total += biomassHere;
            weightMap.put(seaTile, biomassHere);
        }
        //now again, normalize to 1
        double finalTotal = total;
        Preconditions.checkState(total > 0, "nothing to reallocate for " + species);
        weightMap.replaceAll((seaTile, oldbiomass) -> oldbiomass / finalTotal);


        assert checkItIsNormalized();


    }

    private boolean checkItIsNormalized() {
        double total = 0;
        for (Double value : weightMap.values()) {
            total += value;
        }
        return Math.abs(total - 1) <= .0001;
    }

    @Override
    public double allocate(SeaTile tile, NauticalMap map, MersenneTwisterFast random) {

        Preconditions.checkArgument(weightMap != null, "no snapshot taken yet!");
        return weightMap.getOrDefault(tile, 0d);
    }
}
