package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import static java.util.stream.IntStream.range;

public class ActiveOpportunities implements Startable, Steppable {

    private Stoppable stoppable;

    private final Multimap<Integer, Int2D> opportunities = HashMultimap.create();

    public boolean hasOpportunity(Int2D gridLocation, int step) {
        return opportunities.containsEntry(step, gridLocation);
    }

    public void addOpportunity(Int2D gridLocation, int step, int duration) {
        range(step, step + duration).forEach(t -> opportunities.put(t, gridLocation));
    }

    @Override
    public void step(SimState simState) {
        opportunities.removeAll(((FishState) simState).getStep() - 1);
    }

    @Override
    public void start(FishState model) {
        if (stoppable != null)
            throw new IllegalStateException(this + "Already started");
        stoppable = model.scheduleEveryDay(this, StepOrder.DAWN);
    }
}
