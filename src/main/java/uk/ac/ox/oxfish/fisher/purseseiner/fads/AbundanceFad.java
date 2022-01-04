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
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;

import java.util.Arrays;
import java.util.Map;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.ImmutableAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

public class AbundanceFad extends Fad<AbundanceLocalBiology, AbundanceFad> {

    public AbundanceFad(
        final FadManager<AbundanceLocalBiology, AbundanceFad> owner,
        final AbundanceLocalBiology biology,
        final FishAttractor<AbundanceLocalBiology, AbundanceFad> fishAttractor,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed,
        final double totalCarryingCapacity
    ) {
        super(
            owner,
            biology,
            fishAttractor,
            fishReleaseProbability,
            stepDeployed,
            locationDeployed,
            totalCarryingCapacity
        );
    }

    @Override
    public void releaseFish(
        final Iterable<Species> allSpecies,
        final LocalBiology seaTileBiology
    ) {
        if (seaTileBiology instanceof AbundanceLocalBiology) {
            allSpecies.forEach(species -> {
                final double[][] fadAbundance = getBiology().getAbundance(species).asMatrix();
                final double[][] tileAbundance = seaTileBiology.getAbundance(species).asMatrix();
                for (int div = 0; div < fadAbundance.length; div++) {
                    for (int bin = 0; bin < fadAbundance[div].length; bin++) {
                        tileAbundance[div][bin] += fadAbundance[div][bin];
                        fadAbundance[div][bin] = 0;
                    }
                }
            });
        } else {
            releaseFish(allSpecies);
        }
    }

    @Override
    public void releaseFish(final Iterable<Species> allSpecies) {
        final Map<Species, Double> biomassLost = stream(allSpecies)
            .collect(toImmutableMap(identity(), getBiology()::getBiomass));
        getOwner().reactTo(new BiomassLostEvent(biomassLost));
        getOwner().getFadMap().getAbundanceLostObserver().observe(
            new AbundanceLostEvent(ImmutableAbundance.extractFrom(getBiology()))
        );
        // directly reset the biology's abundance arrays to zero
        getBiology().getAbundance().values().stream().flatMap(Arrays::stream)
            .forEach(abundanceArray -> Arrays.fill(abundanceArray, 0));
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
        final GlobalBiology globalBiology
    ) {

        final AbundanceLocalBiology attractedFish =
            getFishAttractor().attract(seaTileBiology, this);

        getBiology().getStructuredAbundance().forEach((species, fadAbundance) -> {
            final double[][] attractedAbundance = attractedFish.getAbundance(species).asMatrix();
            fadAbundance.forEachIndex((sub, bin) ->
                fadAbundance.asMatrix()[sub][bin] += attractedAbundance[sub][bin]
            );
        });

        return new Catch(
            Species.mapToArray(attractedFish.getStructuredAbundance()),
            globalBiology
        );
    }
}
