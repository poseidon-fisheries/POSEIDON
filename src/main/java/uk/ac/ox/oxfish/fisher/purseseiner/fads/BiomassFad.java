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

import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import static java.util.function.Function.identity;

public class BiomassFad extends Fad<BiomassLocalBiology, BiomassFad> {

    public BiomassFad(
        final FadManager<BiomassLocalBiology, BiomassFad> owner,
        final BiomassLocalBiology biology,
        final FishAttractor<BiomassLocalBiology, BiomassFad> fishAttractor,
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

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology. If the local
     * biology is not biomass based (most likely because we're outside the habitable zone), the fish
     * is lost.
     */
    @Override
    public void releaseFish(final Collection<Species> allSpecies, final LocalBiology seaTileBiology) {
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
        final Collection<Species> allSpecies,
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
    public void releaseFish(final Collection<Species> allSpecies) {
        final Map<Species, Double> biomassLost =
            allSpecies.stream().collect(toImmutableMap(identity(), getBiology()::getBiomass));
        getOwner().reactTo(new BiomassLostEvent(biomassLost));
        allSpecies.forEach(species -> getBiology().setCurrentBiomass(species, 0));
    }

    @Override
    public double[] getBiomass() {
        return getBiology().getCurrentBiomass();
    }

    @NotNull
    @Override
    public Catch addCatchesToFad(
        final BiomassLocalBiology seaTileBiology,
        final GlobalBiology globalBiology
    ) {
        final WeightedObject<BiomassLocalBiology> attracted = attractFish(seaTileBiology);
        double[] catches = attracted != null ?
            attracted.getObjectBeingWeighted().getCurrentBiomass() : null;
        if (catches == null)
            catches = new double[globalBiology.getSize()];
        final double[] fadBiomass =
            this.getBiology().getCurrentBiomass();

        for (int i = 0; i < fadBiomass.length; i++) {
            // this mutates the FAD's biomass array directly
            fadBiomass[i] += catches[i];
        }
        return new Catch(catches);
    }

    /**
     * This should be used instead of {@code getBiology().isFull()} since FADs use a global carrying
     * capacity instead of a per-species carrying capacity.
     */
    public boolean isFull() {
        final double totalBiomass = Arrays.stream(getBiology().getCurrentBiomass()).sum();
        return totalBiomass >= getTotalCarryingCapacity();
    }

}
