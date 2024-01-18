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

import com.google.common.base.Preconditions;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.ImmutableAbundance;
import uk.ac.ox.oxfish.biology.complicated.StructuredAbundance;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.util.*;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

public class AbundanceAggregatingFad
    extends AggregatingFad<AbundanceLocalBiology, AbundanceAggregatingFad> {

    public AbundanceAggregatingFad(
        final FadManager owner,
        final AbundanceLocalBiology biology,
        final FishAttractor<AbundanceLocalBiology, AbundanceAggregatingFad> fishAttractor,
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

    @Override
    public void releaseFish(
        final Collection<? extends Species> allSpecies,
        final LocalBiology seaTileBiology
    ) {
        getOwner().reactTo(
            AbundanceFadAttractionEvent.class,
            () -> makeReleaseEvent(allSpecies, seaTileBiology)
        );
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

    private AbundanceFadAttractionEvent makeReleaseEvent(
        final Collection<? extends Species> allSpecies,
        final LocalBiology seaTileBiology
    ) {
        // If we're on a proper abundance tile, we grab that tile's biology directly.
        // Otherwise, we're probably on an empty biology, so we create an empty abundance
        final AbundanceLocalBiology tileAbundanceBefore = seaTileBiology instanceof AbundanceLocalBiology
            ? (AbundanceLocalBiology) seaTileBiology
            : new AbundanceLocalBiology(allSpecies);

        // turn all abundance numbers from the FAD to negative, since we're loosing it.
        // this will be horribly slow but hopefully doesn't happen *too* often
        final AbundanceLocalBiology fadAbundanceDelta = new AbundanceLocalBiology(
            this.getBiology().getStructuredAbundance().entrySet().stream().collect(toImmutableMap(
                Map.Entry::getKey,
                entry -> entry.getValue().mapValues(v -> -v).asMatrix()
            )));

        return new AbundanceFadAttractionEvent(this, tileAbundanceBefore, fadAbundanceDelta);
    }

    @Override
    public void releaseFish(final Collection<? extends Species> allSpecies) {

        double totalBiomassToRelease = 0;
        final Map<Species, Double> biomassLost = new HashMap<>(allSpecies.size());
        for (final Species species : allSpecies) {
            final double biomassHere = getBiology().getBiomass(species);
            totalBiomassToRelease += biomassHere;
            biomassLost.put(species, biomassHere);
        }

        if (totalBiomassToRelease > 0) {

            getOwner().reactTo(new BiomassLostEvent(biomassLost));
            getOwner().getFadMap().getAbundanceLostObserver().observe(
                new AbundanceLostEvent(ImmutableAbundance.extractFrom(getBiology()))
            );
            // directly reset the biology's abundance arrays to zero
            getBiology().getAbundance().values().stream().flatMap(Arrays::stream)
                .forEach(abundanceArray -> Arrays.fill(abundanceArray, 0));
        }
    }

    @Override
    public double[] getBiomass() {
        return getBiology().getCurrentBiomass();
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
        final LocalBiology seaTileBiology,
        final GlobalBiology globalBiology
    ) {

        final WeightedObject<AbundanceLocalBiology> attracted = attractFish(seaTileBiology);

        final AbundanceLocalBiology attractedFish =
            Optional.ofNullable(attracted)
                .map(WeightedObject::getObjectBeingWeighted)
                .orElseGet(() -> new AbundanceLocalBiology(globalBiology));

        if (seaTileBiology instanceof AbundanceLocalBiology) {
            this.getOwner().reactTo(
                new AbundanceFadAttractionEvent(
                    this,
                    (AbundanceLocalBiology) seaTileBiology,
                    attractedFish
                )
            );
        }

        if (attracted == null)
            return null;
        if (attracted.getTotalWeight() < 0) {
            // sometimes it is effectively 0
            assert attracted.getTotalWeight() > -FishStateUtilities.EPSILON;
            Preconditions.checkArgument(attracted.getTotalWeight() > -FishStateUtilities.EPSILON);
            return null;
        }

        getBiology().getStructuredAbundance().forEach((species, fadAbundance) -> {
            final double[][] attractedAbundance = attractedFish.getAbundance(species).asMatrix();
            fadAbundance.forEachIndex((sub, bin) ->
                fadAbundance.asMatrix()[sub][bin] += attractedAbundance[sub][bin]
            );
        });

        final StructuredAbundance[] structuredAbundances = Species
            .mapToList(attractedFish.getStructuredAbundance())
            .toArray(new StructuredAbundance[globalBiology.getSize()]);
        return new Catch(
            structuredAbundances,
            globalBiology
        );
    }
}
