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

import static java.util.stream.Collectors.toMap;
import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.oxfish.geography.currents.CurrentPattern.NEUTRAL;
import static uk.ac.ox.oxfish.utility.Measures.asDouble;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;
import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.currents.CurrentPattern;
import uk.ac.ox.oxfish.geography.currents.CurrentVectors;
import uk.ac.ox.oxfish.geography.currents.CurrentVectorsEPO;

/**
 * Just a bunch of statics to make testing stuff around FADs easier
 */
public class TestUtilities {

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    public static BiomassLocalBiology makeBiology(
        final GlobalBiology globalBiology,
        final Quantity<Mass> carryingCapacity
    ) {
        return makeBiology(globalBiology, asDouble(carryingCapacity, KILOGRAM));
    }

    /**
     * Make a new biology with the given carrying capacity and zero biomass
     */
    public static BiomassLocalBiology makeBiology(
        final GlobalBiology globalBiology,
        final double carryingCapacityValue
    ) {
        final double[] biomass = new double[globalBiology.getSize()];
        Arrays.fill(biomass, 0.0);
        final double[] carryingCapacity = new double[globalBiology.getSize()];
        Arrays.fill(carryingCapacity, carryingCapacityValue);
        return new BiomassLocalBiology(biomass, carryingCapacity);
    }

    public static void fillBiomassFad(final BiomassFad fad) {
        final double[] biomassArray = fad.getBiology().getCurrentBiomass();
        Arrays.fill(biomassArray, fad.getTotalCarryingCapacity() / biomassArray.length);
    }

    public static CurrentVectors makeUniformCurrentVectors(
        final NauticalMap nauticalMap,
        final Double2D currentVector,
        final int stepsPerDay
    ) {
        final Map<Int2D, Double2D> vectors = nauticalMap
            .getAllSeaTilesExcludingLandAsList().stream()
            .collect(toMap(SeaTile::getGridLocation, __ -> currentVector));
        final TreeMap<Integer, EnumMap<CurrentPattern, Map<Int2D, Double2D>>> vectorMaps =
            new TreeMap<>();
        vectorMaps.put(1, new EnumMap<>(ImmutableMap.of(NEUTRAL, vectors)));
        return new CurrentVectorsEPO(
            vectorMaps,
            __ -> NEUTRAL,
            nauticalMap.getWidth(), nauticalMap.getHeight(),
            stepsPerDay
        );
    }
}