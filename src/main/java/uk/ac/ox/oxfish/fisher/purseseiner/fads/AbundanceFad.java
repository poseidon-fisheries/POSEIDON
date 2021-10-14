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

import static uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology.makeAbundanceArray;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.AbundanceFilter;

public class AbundanceFad extends Fad<AbundanceLocalBiology, AbundanceFad> {

    private final Map<Species, AbundanceFilter> selectivityFilters;

    public AbundanceFad(
        final FadManager<AbundanceLocalBiology, AbundanceFad> owner,
        final AbundanceLocalBiology biology,
        final Map<Species, FadBiomassAttractor> fadBiomassAttractors,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed,
        final Map<Species, AbundanceFilter> selectivityFilters,
        final double totalCarryingCapacity
    ) {
        super(
            owner,
            biology,
            fadBiomassAttractors,
            fishReleaseProbability,
            stepDeployed,
            locationDeployed,
            totalCarryingCapacity
        );
        this.selectivityFilters = ImmutableMap.copyOf(selectivityFilters);
    }

    @Override
    public void releaseFish(
        final Iterable<Species> allSpecies, final LocalBiology seaTileBiology
    ) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public void releaseFish(final Iterable<Species> allSpecies) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    double[] getBiomass(final AbundanceLocalBiology biology) {
        return biology.getCurrentBiomass();
    }

    /**
     * This serves two purposes.
     * <ul>
     * <li>It MUTATES the abundance arrays of the FAD's biology.</li>
     * <li>It builds and returns the {@link Catch} object that will be used to remove catches
     * from the cell.</li>
     * </ul>
     */
    @Override
    public Catch addCatchesToFad(
        final AbundanceLocalBiology seaTileBiology,
        final GlobalBiology globalBiology,
        final double[] catches
    ) {
        final int n = globalBiology.getSize();
        final StructuredAbundance[] caughtAbundances = new StructuredAbundance[n];
        final Map<Species, double[][]> fadAbundanceArrays = getBiology().getAbundance();
        for (int i = 0; i < n; i++) {
            final Species species = globalBiology.getSpecie(i);
            final double[][] tileAbundance =
                seaTileBiology.getAbundance(species).asMatrix();
            final double[][] catchableAbundance =
                selectivityFilters.get(species).filter(species, tileAbundance);
            final double[][] caughtAbundance =
                makeAbundanceArray(species);
            final double ratio =
                catches[species.getIndex()] / seaTileBiology.getBiomass(species);
            final double[][] fadAbundanceArray =
                fadAbundanceArrays.get(globalBiology.getSpecie(i));
            for (int div = 0; div < species.getNumberOfSubdivisions(); div++) {
                for (int bin = 0; bin < caughtAbundance[div].length; bin++) {
                    caughtAbundance[div][bin] = ratio * catchableAbundance[div][bin];
                    fadAbundanceArray[div][bin] += caughtAbundance[div][bin];
                }
            }
            caughtAbundances[i] = new StructuredAbundance(caughtAbundance);
        }
        return new Catch(caughtAbundances, globalBiology);
    }
}
