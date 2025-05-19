/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import ec.util.MersenneTwisterFast;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.*;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Map;
import java.util.function.IntSupplier;

public abstract class AggregatingFadInitializer<
    B extends LocalBiology,
    F extends AggregatingFad<B, F>>
    implements FadInitializer<B, F> {

    final double[] emptyBiomasses;
    private final FishAttractor<B, F> fishAttractor;
    private final IntSupplier timeStepSupplier;
    private final GlobalBiology biology;

    private final CarryingCapacitySupplier carryingCapacitySupplier;
    private final Map<Species, Double> fishReleaseProbabilities;

    AggregatingFadInitializer(
        final GlobalBiology globalBiology,
        final FishAttractor<B, F> fishAttractor,
        final IntSupplier timeStepSupplier,
        final CarryingCapacitySupplier carryingCapacitySupplier,
        final Map<Species, Double> fishReleaseProbabilities
    ) {
        this.emptyBiomasses = new double[globalBiology.getSize()];
        this.carryingCapacitySupplier = carryingCapacitySupplier;
        this.fishReleaseProbabilities = ImmutableMap.copyOf(fishReleaseProbabilities);
        this.timeStepSupplier = timeStepSupplier;
        this.fishAttractor = fishAttractor;
        this.biology = globalBiology;
    }

    @Override
    public F makeFad(
        final FadManager fadManager,
        final Fisher owner,
        final SeaTile initialLocation,
        final MersenneTwisterFast rng
    ) {
        return makeFad(
            fadManager,
            makeBiology(biology),
            fishAttractor,
            timeStepSupplier.getAsInt(),
            new Int2D(initialLocation.getGridX(), initialLocation.getGridY()),
            carryingCapacitySupplier.get(),
            fishReleaseProbabilities
        );
    }

    protected abstract F makeFad(
        FadManager owner,
        B biology,
        FishAttractor<B, F> fishAttractor,
        int stepDeployed,
        Int2D locationDeployed,
        CarryingCapacity carryingCapacity,
        Map<Species, Double> fishReleaseProbabilities
    );

    protected abstract B makeBiology(GlobalBiology globalBiology);

}
