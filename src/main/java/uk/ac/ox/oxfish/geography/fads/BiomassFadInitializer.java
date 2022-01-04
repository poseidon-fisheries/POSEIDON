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

package uk.ac.ox.oxfish.geography.fads;

import java.util.function.IntSupplier;
import java.util.stream.DoubleStream;
import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAttractor;

public class BiomassFadInitializer extends FadInitializer<BiomassLocalBiology, BiomassFad> {

    public BiomassFadInitializer(
        final GlobalBiology globalBiology,
        final double totalCarryingCapacity,
        final FishAttractor<BiomassLocalBiology, BiomassFad> fishBiomassAttractor,
        final double fishReleaseProbability,
        final IntSupplier timeStepSupplier
    ) {
        super(
            globalBiology,
            totalCarryingCapacity,
            fishBiomassAttractor,
            fishReleaseProbability,
            timeStepSupplier
        );
    }

    @NotNull
    BiomassLocalBiology makeBiology(final GlobalBiology globalBiology) {
        final double[] carryingCapacities = DoubleStream
            .generate(() -> Double.POSITIVE_INFINITY)
            .limit(globalBiology.getSize())
            .toArray();
        return new BiomassLocalBiology(emptyBiomasses, carryingCapacities);
    }

    @NotNull
    BiomassFad makeFad(
        final FadManager<BiomassLocalBiology, BiomassFad> owner,
        final BiomassLocalBiology biology,
        final FishAttractor<BiomassLocalBiology, BiomassFad> fishBiomassAttractor,
        final double fishReleaseProbability,
        final int stepDeployed,
        final Int2D locationDeployed
    ) {
        return new BiomassFad(
            owner,
            biology,
            fishBiomassAttractor,
            fishReleaseProbability,
            stepDeployed,
            locationDeployed,
            getTotalCarryingCapacity()
        );
    }


}
