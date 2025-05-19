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

package uk.ac.ox.oxfish.biology.complicated;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * memorizes abundance and then redistributes according to given allocator
 * it according to the given allocators
 */
public class AbundanceResetter implements BiologyResetter {

    private final Species species;
    private BiomassAllocator allocator;
    private double[][] recordedAbundance;


    public AbundanceResetter(BiomassAllocator allocator, Species species) {
        this.allocator = allocator;
        this.species = species;
    }


    @Override
    public void recordHowMuchBiomassThereIs(FishState state) {
        recordedAbundance = new double[species.getNumberOfSubdivisions()][species.getNumberOfBins()];

        for (SeaTile seaTile : state.getMap().getAllSeaTilesExcludingLandAsList()) {

            if (!seaTile.isFishingEvenPossibleHere())
                continue;
            StructuredAbundance abundance = seaTile.getAbundance(species);
            for (int i = 0; i < species.getNumberOfSubdivisions(); i++) {
                for (int j = 0; j < species.getNumberOfBins(); j++) {
                    recordedAbundance[i][j] += abundance.asMatrix()[i][j];
                }
            }

        }

    }

    @Override
    public void resetAbundance(
        NauticalMap map,
        MersenneTwisterFast random
    ) {
        for (SeaTile seaTile : map.getAllSeaTilesExcludingLandAsList()) {
            resetAbundanceHere(seaTile, map, random);
        }
    }

    public void resetAbundanceHere(
        SeaTile tile,
        NauticalMap map,
        MersenneTwisterFast random
    ) {

        if (!tile.isFishingEvenPossibleHere()) {
            Preconditions.checkArgument(
                allocator.allocate(tile, map, random) == 0 |
                    Double.isNaN(allocator.allocate(tile, map, random)),
                "Allocating biomass on previously unfishable areas is not allowed; " +
                    "keep them empty but don't use always empty local biologies " + "\n" +
                    allocator.allocate(tile, map, random)
            );
            return;
        }

        double[][] abundanceHere = tile.getAbundance(species).asMatrix();
        assert abundanceHere.length == species.getNumberOfSubdivisions();
        assert abundanceHere[0].length == species.getNumberOfBins();
        double weightHere = allocator.allocate(tile, map, random);

        for (int i = 0; i < species.getNumberOfSubdivisions(); i++) {
            for (int j = 0; j < species.getNumberOfBins(); j++) {
                abundanceHere[i][j] = weightHere * recordedAbundance[i][j];
            }
        }


    }

    public BiomassAllocator getAllocator() {
        return allocator;
    }

    public void setAllocator(BiomassAllocator allocator) {
        this.allocator = allocator;
    }

    public Species getSpecies() {
        return species;
    }
}
