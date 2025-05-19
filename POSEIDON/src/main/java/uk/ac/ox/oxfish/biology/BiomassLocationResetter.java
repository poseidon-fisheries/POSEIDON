/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.biology.initializer.allocator.BiomassAllocator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.HashMap;
import java.util.function.Supplier;

/**
 * calls biomass allocator at the end of each year to reset the location of biomass left.
 * If the habitat doesn't support it, the biomass will die in the transportation
 */
public class BiomassLocationResetter implements AdditionalStartable, Steppable {

    private static final long serialVersionUID = -9211019562781696380L;
    private final Species species;

    //a supplier because I want a "new" biomass allocator each time step
    private final Supplier<BiomassAllocator> biomassAllocator;


    public BiomassLocationResetter(final Species species, final Supplier<BiomassAllocator> biomassAllocator) {
        this.species = species;
        this.biomassAllocator = biomassAllocator;
    }


    public void start(final FishState model) {


        model.scheduleEveryYear(this, StepOrder.AFTER_DATA);
    }


    public void turnOff() {

    }

    @Override
    public void step(final SimState simState) {

        final FishState state = (FishState) simState;

        final BiomassAllocator thisYearAllocator = this.biomassAllocator.get();
        Double totalAllocation = 0d;
        final double totalBiomass = computeBiomassNextYear((FishState) simState);
        final HashMap<SeaTile, Double> hashMap = new HashMap<>();
        //for all the areas of the seas that are livable
        for (final SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList()) {
            //skip if it's unlivable

            if (((VariableBiomassBasedBiology) tile.getBiology()).getCarryingCapacity(species) <= 0)
                continue;


            //allocate new biomass weight
            double allocated = thisYearAllocator.allocate(
                tile,
                state.getMap(),
                state.getRandom()
            );

            if (!Double.isFinite(allocated))
                allocated = 0;
            hashMap.put(
                tile,
                allocated
            );
            totalAllocation += allocated;


        }
        assert Double.isFinite(totalAllocation);
        assert totalAllocation >= 0;

        //now loop again and place it!
        for (final SeaTile tile : state.getMap().getAllSeaTilesExcludingLandAsList()) {
            if (tile.getBiology() instanceof VariableBiomassBasedBiology) {
                final VariableBiomassBasedBiology biology = (VariableBiomassBasedBiology) tile.getBiology();
                if (biology.getCarryingCapacity(species) > 0) {
                    biology.setCurrentBiomass(
                        species,
                        Math.min(
                            totalBiomass * hashMap.get(tile) / totalAllocation,
                            biology.getCarryingCapacity(species)
                        )
                    );
                }

            }


        }
    }

    protected double computeBiomassNextYear(final FishState simState) {
        return simState.getTotalBiomass(species);
    }
}
