package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.fisher.actions.fads.DeployFad;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;

import javax.measure.Quantity;
import javax.measure.quantity.Volume;

import static com.google.common.base.Preconditions.checkArgument;

public class ActiveFadsLimit implements ActionSpecificRegulation {

    private final ImmutableSet<Class<? extends FadAction>> applicableActions = ImmutableSet.of(DeployFad.class);
    private final ImmutableSortedMap<Integer, Integer> limits;

    public ActiveFadsLimit(ImmutableSortedMap<Integer, Integer> limits) {
        checkArgument(limits.keySet().contains(0));
        this.limits = limits;
    }

    @Override public ImmutableSet<Class<? extends FadAction>> getApplicableActions() { return applicableActions; }

    public boolean isAllowed(FadAction action) {
        assert applicableActions.contains(action.getClass());
        return action.getFisher().getHold()
            .getVolume()
            .map(holdVolume -> action.getFadManager().getNumDeployedFads() < getLimit(holdVolume))
            .orElseThrow(() -> new IllegalArgumentException(
                "Hold volume needs to be known to get active FAD limit for fisher " + action.getFisher()
            ));
    }

    public int getLimit(Quantity<Volume> holdVolume) {
        return getLimit(holdVolume.toSystemUnit().getValue().intValue());
    }

    public int getLimit(int holdVolume) {
        checkArgument(holdVolume > 0);
        return limits.floorEntry(holdVolume).getValue();
    }
}
