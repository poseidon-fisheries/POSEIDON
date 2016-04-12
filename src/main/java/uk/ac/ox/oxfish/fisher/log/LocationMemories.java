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
public class LocationMemories<T> implements Startable, Steppable
{


    /**
     * a map of each sea-tile with its most recent memory
     */
    private final Map<SeaTile,LocationMemory<T>> memories;

    private final double dailyForgettingProbability;

    private final int minimumMemoryAgeInDaysBeforeForgetting;


    private final int minimumNumberOfMemoriesBeforeForgetting;


    public LocationMemories(
            double dailyForgettingProbability,
            int minimumMemoryAgeInDaysBeforeForgetting,
            int minimumNumberOfMemoriesBeforeForgetting) {
        this.memories = new HashMap<>();
        this.dailyForgettingProbability = dailyForgettingProbability;
        this.minimumMemoryAgeInDaysBeforeForgetting = minimumMemoryAgeInDaysBeforeForgetting;
        this.minimumNumberOfMemoriesBeforeForgetting = minimumNumberOfMemoriesBeforeForgetting;
    }

    private Stoppable receipt;

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        receipt = model.scheduleEveryDay(this, StepOrder.DATA_RESET);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        receipt.stop();
    }

    @Override
    public void step(SimState simState)
    {

        LinkedList<SeaTile> toRemove = new LinkedList<>();

        for(Map.Entry<SeaTile,LocationMemory<T>> memory : memories.entrySet() )
        {
            int age = memory.getValue().age();
            if(age >= minimumMemoryAgeInDaysBeforeForgetting &&
                    memories.size() > minimumNumberOfMemoriesBeforeForgetting &&
                    ((FishState) simState).getRandom().nextBoolean(dailyForgettingProbability))
                toRemove.add(memory.getKey());

        }

        toRemove.forEach(memories::remove);

    }

    /**
     * grab the location whose memory is "best" according to the given comparator
     */
    public SeaTile getBestFishingSpotInMemory(Comparator<LocationMemory<T>> comparator)
    {

        Optional<LocationMemory<T>> best = memories.values().stream().max(comparator);

        if(best.isPresent())
            return best.get().getSpot();
        else
            return null;
    }

    /**
     * memorize a new event!
     * @param memory what to remember
     */
    public void memorize(T memory, SeaTile tile)
    {
        memories.put(tile, new LocationMemory<>(tile, memory));
    }

    /**

     */
    public T getMemory(SeaTile key) {
        LocationMemory<T> memory = memories.get(key);
        if(memory != null)
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





