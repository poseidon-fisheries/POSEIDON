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
import ec.util.MersenneTwisterFast;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.fads.Fad;
import uk.ac.ox.oxfish.fisher.equipment.fads.FadManager;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Map;
import java.util.function.Function;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

public class FadInitializer implements Function<FadManager, Fad> {

    private final double[] emptyBiomasses;
    private final double[] carryingCapacities;
    private final double fishReleaseProbability;
    private final ImmutableMap<Species, Double> attractionRates;
    private final MersenneTwisterFast rng;
    private final double dudProbability;

    public FadInitializer(
        GlobalBiology globalBiology,
        Map<Species, Quantity<Mass>> carryingCapacities,
        Map<Species, Double> attractionRates,
        MersenneTwisterFast rng, double fishReleaseProbability,
        double dudProbability
    ) {
        this.emptyBiomasses = new double[globalBiology.getSize()];
        this.carryingCapacities = new double[globalBiology.getSize()];
        this.rng = rng;
        this.dudProbability = dudProbability;
        carryingCapacities.forEach((species, qty) ->
            this.carryingCapacities[species.getIndex()] = asDouble(qty, KILOGRAM)
        );
        this.attractionRates = ImmutableMap.copyOf(attractionRates);
        this.fishReleaseProbability = fishReleaseProbability;
    }

    @Override public Fad apply(@NotNull FadManager fadManager) {
        return new Fad(
            fadManager,
            new BiomassLocalBiology(emptyBiomasses, carryingCapacities),
            rng.nextBoolean(dudProbability) ? ImmutableMap.of() : this.attractionRates,
            fishReleaseProbability
        );
    }

}
