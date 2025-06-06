/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.log;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.*;

/**
 * Basically a table holding for each sea-tile a specific memory (a trip, or a location or something else).
 * It steps itself aging the memories contained and has a fixed chance each day of forgetting them
 * Created by carrknight on 7/27/15.
 */
public class LocationMemories<T> implements Startable, Steppable {


    private static final long serialVersionUID = 3423123730692395603L;
    /**
     * a map of each sea-tile with its most recent memory
     */
    private final Map<SeaTile, LocationMemory<T>> memories;

    private final double dailyForgettingProbability;

    private final int minimumMemoryAgeInDaysBeforeForgetting;


    private final int minimumNumberOfMemoriesBeforeForgetting;
    private Stoppable receipt;

    public LocationMemories(
        final double dailyForgettingProbability,
        final int minimumMemoryAgeInDaysBeforeForgetting,
        final int minimumNumberOfMemoriesBeforeForgetting
    ) {
        this.memories = new HashMap<>();
        this.dailyForgettingProbability = dailyForgettingProbability;
        this.minimumMemoryAgeInDaysBeforeForgetting = minimumMemoryAgeInDaysBeforeForgetting;
        this.minimumNumberOfMemoriesBeforeForgetting = minimumNumberOfMemoriesBeforeForgetting;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        receipt = model.scheduleEveryYear(this, StepOrder.DATA_RESET);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
    }

    @Override
    public void step(final SimState simState) {

        final LinkedList<SeaTile> toRemove = new LinkedList<>();

        if (memories.size() > minimumNumberOfMemoriesBeforeForgetting) {
            for (final Map.Entry<SeaTile, LocationMemory<T>> memory : memories.entrySet()) {
                final int age = memory.getValue().age();
                if (age >= minimumMemoryAgeInDaysBeforeForgetting)
                    if (dailyForgettingProbability >= 1 ||
                        ((FishState) simState).getRandom().nextBoolean(dailyForgettingProbability))
                        toRemove.add(memory.getKey());

            }


            toRemove.forEach(memories::remove);
        }
    }

    /**
     * grab the location whose memory is "best" according to the given comparator
     */
    public SeaTile getBestFishingSpotInMemory(final Comparator<LocationMemory<T>> comparator) {

        final Optional<LocationMemory<T>> best = memories.values().stream().max(comparator);

        if (best.isPresent())
            return best.get().getSpot();
        else
            return null;
    }

    /**
     * memorize a new event!
     *
     * @param memory what to remember
     */
    public void memorize(final T memory, final SeaTile tile) {
        memories.put(tile, new LocationMemory<>(tile, memory));
    }

    /**
     *
     */
    public T getMemory(final SeaTile key) {
        final LocationMemory<T> memory = memories.get(key);
        if (memory != null)
            return memory.getInformation();
        else
            return null;
    }


    /**
     * Getter for property 'memories'.
     *
     * @return Value for property 'memories'.
     */
    public Map<SeaTile,
        LocationMemory<T>> getMemories() {
        return memories;
    }
}





