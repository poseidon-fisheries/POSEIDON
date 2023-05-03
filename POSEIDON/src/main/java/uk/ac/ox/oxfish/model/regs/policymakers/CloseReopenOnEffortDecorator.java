package uk.ac.ox.oxfish.model.regs.policymakers;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.plugins.EntryPlugin;
import uk.ac.ox.oxfish.utility.adaptation.Actuator;

/**
 * Whenever the target policy is 1 or above, make sure all the limits on new entrants are removed.
 * Viceversa when policy is below 1, make sure no new entrants are allowed
 */
public class CloseReopenOnEffortDecorator implements Actuator<FishState, Double> {


    private final  Actuator<FishState,Double> delegate;

    public CloseReopenOnEffortDecorator(Actuator<FishState, Double> delegate) {
        this.delegate = delegate;
    }

    @Override
    public void apply(FishState subject, Double policy, FishState model) {

        if(policy>=1)
            for (EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(false);
            }
        if(policy<1)
            for (EntryPlugin entryPlugin : model.getEntryPlugins()) {
                entryPlugin.setEntryPaused(true);
            }

        delegate.apply(subject, policy, model);
    }
}
