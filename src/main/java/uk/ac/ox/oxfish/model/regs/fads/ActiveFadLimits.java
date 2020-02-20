package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.fisher.actions.fads.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;

public class ActiveFadLimits implements ActionSpecificRegulation {

    private final ImmutableSet<Class<? extends FadAction>> applicableActions = ImmutableSet.of(DeployFad.class);
    private final VolumeRelativeLimits limits;

    public ActiveFadLimits(ImmutableSortedMap<Integer, Integer> limits) {
        this.limits = new VolumeRelativeLimits(limits);
    }

    @Override public ImmutableSet<Class<? extends FadAction>> getApplicableActions() { return applicableActions; }

    public boolean isAllowed(FadAction action) {
        assert applicableActions.contains(action.getClass());
        return action.getFadManager().getNumDeployedFads() < limits.getLimit(action.getFisher());
    }

}
