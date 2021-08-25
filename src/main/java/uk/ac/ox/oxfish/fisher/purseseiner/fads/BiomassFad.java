/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import static java.util.function.Function.identity;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.model.data.monitors.regions.Locatable;

public class BiomassFad extends Fad<BiomassLocalBiology, BiomassFad> {

    private final Map<Species, FadBiomassAttractor> fadBiomassAttractors;

    public BiomassFad(
        final FadManager<BiomassLocalBiology, BiomassFad> owner,
        final BiomassLocalBiology biology,
        final Map<Species, FadBiomassAttractor> fadBiomassAttractors,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed
    ) {
        super(
            owner,
            biology,
            fishReleaseProbability,
            stepDeployed,
            locationDeployed
        );
        this.fadBiomassAttractors = ImmutableMap.copyOf(fadBiomassAttractors);
    }

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology. If the local
     * biology is not biomass based (most likely because we're outside the habitable zone), the fish
     * is lost.
     */
    @Override
    public void releaseFish(final Iterable<Species> allSpecies, final LocalBiology seaTileBiology) {
        if (seaTileBiology instanceof VariableBiomassBasedBiology) {
            releaseFish(allSpecies, (VariableBiomassBasedBiology) seaTileBiology);
        } else {
            releaseFish(allSpecies);
        }
    }

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology. In the
     * unlikely event that the sea tile's carrying capacity is exceeded, the extra fish is lost.
     */
    private void releaseFish(
        final Iterable<Species> allSpecies,
        final VariableBiomassBasedBiology seaTileBiology
    ) {
        allSpecies.forEach(species -> {
            final double seaTileBiomass = seaTileBiology.getBiomass(species);
            final double fadBiomass = getBiology().getBiomass(species);
            final double totalSeaTileCapacity = seaTileBiology.getCarryingCapacity(species);
            final double availableSeaTileCapacity = totalSeaTileCapacity - seaTileBiomass;
            final double biomassToTransfer = min(availableSeaTileCapacity, fadBiomass);
            getBiology().setCurrentBiomass(species, max(0, fadBiomass - biomassToTransfer));
            seaTileBiology.setCurrentBiomass(
                species,
                min(totalSeaTileCapacity, seaTileBiomass + biomassToTransfer)
            );
        });
        // release whatever is left in the FAD if the sea tile could not absorb it
        releaseFish(allSpecies);
    }

    /**
     * Remove biomass for all the given species from the FAD without sending it anywhere, therefore
     * losing the fish.
     */
    @Override
    public void releaseFish(final Iterable<Species> allSpecies) {
        final Map<Species, Double> biomassLost =
            stream(allSpecies).collect(toImmutableMap(identity(), getBiology()::getBiomass));
        getOwner().reactTo(new BiomassLostEvent(biomassLost));
        allSpecies.forEach(species -> getBiology().setCurrentBiomass(species, 0));
    }

    @Override
    public void aggregateFish(
        final BiomassLocalBiology seaTileBiology,
        final GlobalBiology globalBiology
    ) {
        // Calculate the catches and add them to the FAD biology:
        final double[] tileBiomass = seaTileBiology.getCurrentBiomass();
        final double[] fadBiomass = this.getBiology().getCurrentBiomass();
        final double[] catches = new double[tileBiomass.length];
        final double totalFadBiomass = Arrays.stream(fadBiomass).sum();

        fadBiomassAttractors.forEach((species, fadBiomassAttractor) -> {
            final int i = species.getIndex();
            if (fadBiomassAttractor.shouldAttract(tileBiomass[i], totalFadBiomass)) {
                catches[i] = fadBiomassAttractor.biomassAttracted(
                    tileBiomass[i],
                    fadBiomass[i],
                    totalFadBiomass
                );
                fadBiomass[i] =
                    min(fadBiomass[i] + catches[i], fadBiomassAttractor.getCarryingCapacity());
            }
        });
        // Remove the catches from the underlying biology:
        final Catch catchObject = new Catch(catches);
        seaTileBiology.reactToThisAmountOfBiomassBeingFished(
            catchObject,
            catchObject,
            globalBiology
        );
    }

}
