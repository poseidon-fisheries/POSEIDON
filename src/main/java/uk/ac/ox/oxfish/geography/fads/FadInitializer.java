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

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadBiomassAttractor;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.IntSupplier;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.Comparator.comparingInt;

public class FadInitializer implements Function<FadManager, BiomassFad> {

    private final double[] emptyBiomasses;
    private final Collection<DoubleSupplier> carryingCapacitySuppliers;
    private final double fishReleaseProbability;
    private final Map<Species, FadBiomassAttractor> fadBiomassAttractors;
    private final IntSupplier timeStepSupplier;

    public FadInitializer(
        final GlobalBiology globalBiology,
        final Map<Species, DoubleSupplier> carryingCapacitySuppliers,
        final Map<Species, FadBiomassAttractor> fadBiomassAttractors,
        final double fishReleaseProbability,
        final IntSupplier timeStepSupplier
    ) {
        this.emptyBiomasses = new double[globalBiology.getSize()];
        this.carryingCapacitySuppliers =
            carryingCapacitySuppliers
                .entrySet()
                .stream()
                .sorted(comparingInt(entry -> entry.getKey().getIndex())) // order by species index
                .map(Map.Entry::getValue)
                .collect(toImmutableList());
        this.timeStepSupplier = timeStepSupplier;
        this.fadBiomassAttractors = ImmutableMap.copyOf(fadBiomassAttractors);
        this.fishReleaseProbability = fishReleaseProbability;
    }

    @Override
    public BiomassFad apply(@NotNull final FadManager fadManager) {
        final SeaTile seaTile = fadManager.getFisher().getLocation();
        final double[] carryingCapacities =
            carryingCapacitySuppliers.stream()
                .mapToDouble(DoubleSupplier::getAsDouble)
                .toArray();
        return new BiomassFad(
            fadManager,
            new BiomassLocalBiology(emptyBiomasses, carryingCapacities),
            fadBiomassAttractors,
            fishReleaseProbability,
            timeStepSupplier.getAsInt(),
            new Int2D(seaTile.getGridX(), seaTile.getGridY())
        );
    }

}
