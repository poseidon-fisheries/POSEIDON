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

import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Collection;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;
import static java.util.function.Function.identity;

public class BiomassAggregatingFad
    extends AggregatingFad<BiomassLocalBiology, BiomassAggregatingFad> {

    public BiomassAggregatingFad(
        final FadManager owner,
        final BiomassLocalBiology biology,
        final FishAttractor<BiomassLocalBiology, BiomassAggregatingFad> fishAttractor,
        final int stepDeployed,
        final Int2D locationDeployed,
        final CarryingCapacity carryingCapacity,
        final Map<Species, Double> fishReleaseProbabilities
    ) {
        super(
            owner,
            biology,
            fishAttractor,
            stepDeployed,
            locationDeployed,
            carryingCapacity,
            fishReleaseProbabilities
        );
    }

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology. If the local biology is not
     * biomass based (most likely because we're outside the habitable zone), the fish is lost.
     */
    @Override
    public void releaseFishIntoTile(
        final Collection<? extends Species> speciesToRelease,
        final LocalBiology seaTileBiology
    ) {
        if (seaTileBiology instanceof VariableBiomassBasedBiology) {
            releaseFish(speciesToRelease, (VariableBiomassBasedBiology) seaTileBiology);
        } else {
            releaseFishIntoTheVoid(speciesToRelease);
        }
    }

    /**
     * Remove biomass from the FAD and send the biomass down to the sea tile's biology. In the unlikely event that the
     * sea tile's carrying capacity is exceeded, the extra fish is lost.
     */
    private void releaseFish(
        final Collection<? extends Species> allSpecies,
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
        releaseFishIntoTheVoid(allSpecies);
    }

    /**
     * Remove biomass for all the given species from the FAD without sending it anywhere, therefore losing the fish.
     */
    @Override
    public void releaseFishIntoTheVoid(final Collection<? extends Species> speciesToRelease) {
        final Map<Species, Double> biomassLost =
            speciesToRelease.stream().collect(toImmutableMap(identity(), getBiology()::getBiomass));
        getOwner().reactTo(new BiomassLostEvent(biomassLost));
        speciesToRelease.forEach(species -> getBiology().setCurrentBiomass(species, 0));
    }

    @Override
    public double[] getBiomass() {
        return getBiology().getCurrentBiomass();
    }

    @Override
    public Catch addCatchesToFad(
        final LocalBiology seaTileBiology,
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

}
