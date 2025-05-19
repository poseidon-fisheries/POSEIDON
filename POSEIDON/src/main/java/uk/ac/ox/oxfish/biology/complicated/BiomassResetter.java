/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

public class BiomassResetter implements BiologyResetter {

    /**
     * biomass allocator: I expect this to be normalized!
     */
    private final BiomassAllocator normalizedAllocator;

    private final Species species;

    private double recordedBiomass = Double.NaN;

    private FadMap fadMap = null;

    public BiomassResetter(final BiomassAllocator normalizedAllocator, final Species species) {
        this.normalizedAllocator = normalizedAllocator;
        this.species = species;
    }

    /**
     * records how much biomass there is
     *
     * @param fishState
     */
    @Override
    public void recordHowMuchBiomassThereIs(final FishState fishState) {
        fadMap = fishState.getFadMap();
        recordedBiomass = fishState.getMap().getTotalBiomass(species);
    }

    /**
     * returns biology layer to biomass recorded previously;
     * if above carrying capacity, it kills the fish
     *
     * @param map
     * @param random
     */
    @Override
    public void resetAbundance(final NauticalMap map, final MersenneTwisterFast random) {

        Preconditions.checkState(Double.isFinite(recordedBiomass), "can't reset without recording!");

        final double totalBiomassToAllocate = (fadMap == null)
            ? recordedBiomass
            : recordedBiomass - fadMap.getTotalBiomass(species);

        for (final SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {

            if (!seaTile.isFishingEvenPossibleHere()) {

                Preconditions.checkArgument(
                    normalizedAllocator.allocate(seaTile, map, random) == 0 |
                        Double.isNaN(normalizedAllocator.allocate(seaTile, map, random)),
                    "Allocating biomass on previously unfishable areas is not allowed; " +
                        "keep them empty but don't use always empty local biologies " + "\n" +
                        normalizedAllocator.allocate(seaTile, map, random)
                );

                continue;
            }
            final VariableBiomassBasedBiology biology =
                ((VariableBiomassBasedBiology) seaTile.getBiology());

            final double newBiomass = Math.min(
                totalBiomassToAllocate *
                    normalizedAllocator.allocate(seaTile, map, random),
                biology.getCarryingCapacity(species)

            );
            biology.setCurrentBiomass(
                species,
                newBiomass
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
