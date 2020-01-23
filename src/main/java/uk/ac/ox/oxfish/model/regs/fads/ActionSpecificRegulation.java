package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSet;
import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;

public interface ActionSpecificRegulation extends Startable, Steppable {
    ImmutableSet<Class<? extends FadAction>> getApplicableActions();
    boolean isAllowed(FadAction action);
    default void reactToAction(FadAction action) {}
    @Override default void step(SimState simState) {}
    @Override default void start(FishState model) {}
}
