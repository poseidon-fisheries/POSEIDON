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

import com.google.common.collect.ImmutableMap;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.junit.Assert.assertEquals;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

/**
 * Just a bunch of statics to make testing stuff around FADs easier
 */
public class TestUtilities {

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    public static BiomassLocalBiology makeBiology(GlobalBiology globalBiology, Quantity<Mass> carryingCapacity) {
        return makeBiology(globalBiology, asDouble(carryingCapacity, KILOGRAM));
    }

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    public static BiomassLocalBiology makeBiology(GlobalBiology globalBiology, double carryingCapacityValue) {
        double[] biomass = new double[globalBiology.getSize()];
        Arrays.fill(biomass, 0.0);
        double[] carryingCapacity = new double[globalBiology.getSize()];
        Arrays.fill(carryingCapacity, carryingCapacityValue);
        return new BiomassLocalBiology(biomass, carryingCapacity);
    }

    public static void fillBiology(VariableBiomassBasedBiology biology) {
        final double[] biomassArray = biology.getCurrentBiomass();
        for (int i = 0; i < biomassArray.length; i++)
            biomassArray[i] = biology.getCarryingCapacity(i);
    }

    public static CurrentVectors makeUniformCurrentVectors(
        NauticalMap nauticalMap,
        Double2D currentVector,
        int stepsPerDay
    ) {
        final Map<SeaTile, Double2D> vectors = nauticalMap
            .getAllSeaTilesExcludingLandAsList().stream()
            .collect(toMap(identity(), __ -> currentVector));
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<SeaTile, Double2D>>> vectorMaps = new TreeMap<>();
        vectorMaps.put(1, new EnumMap<>(ImmutableMap.of(NEUTRAL, vectors)));
        return new CurrentVectors(vectorMaps, __ -> NEUTRAL, stepsPerDay);
    }
}
