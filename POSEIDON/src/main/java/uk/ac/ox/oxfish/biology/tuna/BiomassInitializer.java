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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.*;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.SingleSpeciesBiomassInitializer;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * Initializes the biology for multiple independent species and schedules a {@link
 * BiomassReallocator} to run at every step. This class is similar to {@link
 * uk.ac.ox.oxfish.biology.initializer.MultipleIndependentSpeciesBiomassInitializer}, but it doesn't
 * allow imaginary species or movement rules.
 */
public class BiomassInitializer implements BiologyInitializer {

    private final List<SingleSpeciesBiomassInitializer> initializers;

    public BiomassInitializer(
        final Collection<SingleSpeciesBiomassInitializer> initializers
    ) {
        this.initializers = ImmutableList.copyOf(initializers);
    }

    /**
     * if at least one species can live here, return a localBiomassBiology; else return a empty
     * globalBiology.
     */
    @Override
    public LocalBiology generateLocal(
        final GlobalBiology globalBiology,
        final SeaTile seaTile,
        final MersenneTwisterFast random,
        final int mapHeightInCells,
        final int mapWidthInCells,
        final NauticalMap map
    ) {
        assert !initializers.isEmpty();

        final ImmutableList<LocalBiology> localBiologies =
            initializers.stream()
                .map(initializer ->
                    // We need to call `generateLocal` on each initializer for its side-effects
                    initializer.generateLocal(
                        globalBiology,
                        seaTile,
                        random,
                        mapHeightInCells,
                        mapWidthInCells,
                        map
                    )
                )
                .collect(toImmutableList());

        // Just return the first BiomassLocalBiology, it doesn't matter which
        return localBiologies.stream()
            .filter(biology -> biology instanceof BiomassLocalBiology)
            .findFirst()
            .orElse(new EmptyLocalBiology());
    }

    @Override
    public void processMap(
        final GlobalBiology biology,
        final NauticalMap map,
        final MersenneTwisterFast random,
        final FishState fishState
    ) {
        initializers.forEach(initializer -> {
            initializer.setForceMovementOff(true);
            initializer.processMap(biology, map, random, fishState);
        });
    }

    @Override
    public GlobalBiology generateGlobal(final MersenneTwisterFast rng, final FishState fishState) {
        return new GlobalBiology(
            initializers.stream()
                .map(initializer -> initializer.generateGlobal(rng, fishState).getSpecie(0))
                .toArray(Species[]::new)
        );
    }
}
