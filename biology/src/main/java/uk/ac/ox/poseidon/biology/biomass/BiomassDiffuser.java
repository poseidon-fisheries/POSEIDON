/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.biology.biomass;

import ec.util.MersenneTwisterFast;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Double2D;
import sim.util.Int2D;

import java.io.Serial;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toMap;

public class BiomassDiffuser implements Steppable {

    @Serial private static final long serialVersionUID = 6904676724631409234L;

    private final BiomassGrid biomassGrid;

    private final CarryingCapacityGrid carryingCapacityGrid;

    private final Random rng;

    private final BiomassDiffusionRule biomassDiffusionRule;

    private final List<Int2D> habitableLocations;

    private final Map<Int2D, List<Int2D>> habitableNeighbours;

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public BiomassDiffuser(
        final BiomassGrid biomassGrid,
        final CarryingCapacityGrid carryingCapacityGrid,
        final BiomassDiffusionRule biomassDiffusionRule,
        final MersenneTwisterFast rng
    ) {
        checkArgument(biomassGrid.getGridExtent().equals(carryingCapacityGrid.getGridExtent()));
        this.biomassGrid = biomassGrid;
        this.carryingCapacityGrid = carryingCapacityGrid;
        this.biomassDiffusionRule = biomassDiffusionRule;
        this.rng = new Random(rng.nextLong());
        // we copy the habitable locations list because we are going to shuffle it repeatedly
        this.habitableLocations = new ArrayList<>(carryingCapacityGrid.getHabitableCells());
        final HashSet<Int2D> habitableLocationsSet = new HashSet<>(habitableLocations);
        this.habitableNeighbours =
            habitableLocations
                .stream()
                .collect(toMap(
                    identity(),
                    location ->
                        carryingCapacityGrid
                            .getGridExtent()
                            .getNeighbours(location)
                            .stream()
                            .filter(habitableLocationsSet::contains)
                            .collect(toCollection(ArrayList::new))
                ));
    }

    @Override
    public void step(final SimState simState) {
        Collections.shuffle(habitableLocations, rng);
        habitableLocations.forEach(location -> {
            final List<Int2D> neighbours = habitableNeighbours.get(location);
            Collections.shuffle(neighbours, rng);
            neighbours.forEach(neighbour -> {
                final Double2D updatedBiomasses =
                    biomassDiffusionRule.updatedBiomasses(
                        biomassGrid.getDouble(location),
                        carryingCapacityGrid.getCarryingCapacity(location),
                        biomassGrid.getDouble(neighbour),
                        carryingCapacityGrid.getCarryingCapacity(neighbour)
                    );
                biomassGrid.setBiomass(location, updatedBiomasses.x);
                biomassGrid.setBiomass(neighbour, updatedBiomasses.y);
            });
        });
    }
}
