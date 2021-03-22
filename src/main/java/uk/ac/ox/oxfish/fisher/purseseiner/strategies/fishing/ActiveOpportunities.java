package uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Int2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import static java.util.stream.IntStream.range;

public class ActiveOpportunities implements Startable, Steppable {

    private final Multimap<Integer, Int2D> opportunities = HashMultimap.create();
    private boolean isStarted = false;

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
        if (isStarted) throw new IllegalStateException(this + "Already started");
        model.scheduleEveryDay(this, StepOrder.DAWN);
        isStarted = true;
    }
}
