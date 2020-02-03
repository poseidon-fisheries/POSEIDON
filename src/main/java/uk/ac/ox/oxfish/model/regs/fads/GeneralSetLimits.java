package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import sim.engine.SimState;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeUnassociatedSet;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

public class GeneralSetLimits implements ActionSpecificRegulation {

    private final ImmutableSet<Class<? extends FadAction>> applicableActions = ImmutableSet.of(
        MakeFadSet.class, MakeUnassociatedSet.class
    );
    private final VolumeRelativeLimits limits;
    private int setCounter = 0;

    public GeneralSetLimits(ImmutableSortedMap<Integer, Integer> limits) {
        this.limits = new VolumeRelativeLimits(limits);
    }

    @Override public ImmutableSet<Class<? extends FadAction>> getApplicableActions() { return applicableActions; }

    @Override public boolean isAllowed(FadAction action) {
        assert applicableActions.contains(action.getClass());
        return action.getFisher().getHold()
            .getVolume()
            .map(holdVolume -> setCounter < limits.getLimit(holdVolume))
            .orElseThrow(() -> new IllegalArgumentException(
                "Hold volume needs to be known to get general set limit for fisher " + action.getFisher()
            ));
    }

    @Override public void reactToAction(FadAction action) {
        assert applicableActions.contains(action.getClass());
        setCounter++;
    }

    @Override public void step(SimState simState) {
        setCounter = 0;
    }

    @Override public void start(FishState model) {
        model.scheduleEveryYear(this, StepOrder.POLICY_UPDATE);
    }
}
