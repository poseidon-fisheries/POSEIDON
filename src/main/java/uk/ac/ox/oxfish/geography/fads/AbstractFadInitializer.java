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

package uk.ac.ox.oxfish.geography.fads;

import java.util.function.DoubleSupplier;
import java.util.function.IntSupplier;
import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FishAttractor;
import uk.ac.ox.oxfish.geography.SeaTile;

public abstract class AbstractFadInitializer<B extends LocalBiology, F extends Fad<B, F>>
    implements FadInitializer<B, F> {

    final double[] emptyBiomasses;
    private final double fishReleaseProbability;
    private final FishAttractor<B, F> fishAttractor;
    private final IntSupplier timeStepSupplier;
    private final DoubleSupplier carryingCapacityGenerator;
    private final GlobalBiology biology;


    /**
     * abstract fad initializer with fixed carrying capacity for all fads
     */
    AbstractFadInitializer(
        final GlobalBiology globalBiology,
        final double totalCarryingCapacity,
        final FishAttractor<B, F> fishAttractor,
        final double fishReleaseProbability,
        final IntSupplier timeStepSupplier
    ) {
        this(
                globalBiology,
                () -> totalCarryingCapacity,
                fishAttractor,
                fishReleaseProbability,
                timeStepSupplier
        );
    }

    /**
     * abstract fad initializer with fixed carrying capacity for all fads
     */
    AbstractFadInitializer(
            final GlobalBiology globalBiology,
            final DoubleSupplier carryingCapacityGenerator,
            final FishAttractor<B, F> fishAttractor,
            final double fishReleaseProbability,
            final IntSupplier timeStepSupplier
    ) {
        this.emptyBiomasses = new double[globalBiology.getSize()];
        this.timeStepSupplier = timeStepSupplier;
        this.fishAttractor = fishAttractor;
        this.fishReleaseProbability = fishReleaseProbability;
        this.carryingCapacityGenerator = carryingCapacityGenerator;
        this.biology = globalBiology;

    }


    @Override
    public F makeFad(@NotNull final FadManager<B, F> fadManager,
                     Fisher owner,
                     SeaTile initialLocation) {
        return makeFad(
            fadManager,
            makeBiology(biology),
            fishAttractor,
            fishReleaseProbability,
            timeStepSupplier.getAsInt(),
            new Int2D(initialLocation.getGridX(), initialLocation.getGridY())
        );
    }


    protected abstract F makeFad(
            FadManager<B, F> owner,
            B biology,
            FishAttractor<B, F> fishAttractor,
            double fishReleaseProbability,
            int stepDeployed,
            Int2D locationDeployed
    );


    protected abstract B makeBiology(GlobalBiology globalBiology);

    public double generateCarryingCapacity() {
        return carryingCapacityGenerator.getAsDouble();
    }
}