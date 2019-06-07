/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;

public class BiomassResetter implements BiologyResetter {

    /**
     * biomass allocator: I expect this to be normalized!
     */
    private final BiomassAllocator normalizedAllocator;

    private final Species species;

    private double recordedBiomass = Double.NaN;


    public BiomassResetter(BiomassAllocator normalizedAllocator, Species species) {
        this.normalizedAllocator = normalizedAllocator;
        this.species = species;
    }

    /**
     * records how much biomass there is
     *
     * @param map
     */
    @Override
    public void recordAbundance(NauticalMap map) {
        recordedBiomass = map.getTotalBiology(species);
    }

    /**
     * returns biology layer to biomass recorded previously;
     * if above carrying capacity, it kills the fish
     *
     * @param map
     * @param random
     */
    @Override
    public void resetAbundance(NauticalMap map, MersenneTwisterFast random) {

        Preconditions.checkState(Double.isFinite(recordedBiomass),"can't reset without recording!");

        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {

            if(!seaTile.isFishingEvenPossibleHere()) {

                Preconditions.checkArgument(normalizedAllocator.allocate(seaTile,map,random)==0 |
                                                    Double.isNaN(normalizedAllocator.allocate(seaTile,map,random)),
                                            "Allocating biomass on previously unfishable areas is not allowed; " +
                                                    "keep them empty but don't use always empty local biologies " + "\n" +
                                                    normalizedAllocator.allocate(seaTile,map,random));

                continue;
            }
            VariableBiomassBasedBiology biology =
                    ((VariableBiomassBasedBiology) seaTile.getBiology());

            biology.setCurrentBiomass(species,
                                      recordedBiomass *
                                              normalizedAllocator.allocate(seaTile,map,random)

                                      );
        }

    }

    /**
     * species we are resetting
     *
     * @return
     */
    @Override
    public Species getSpecies() {
        return species;
    }
}
