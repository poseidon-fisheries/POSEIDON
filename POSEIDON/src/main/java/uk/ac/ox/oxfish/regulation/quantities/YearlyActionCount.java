package uk.ac.ox.oxfish.regulation.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.maybeGetFadManager;

public class YearlyActionCount implements AlgorithmFactory<Quantity> {

    private StringParameter actionCode;

    public YearlyActionCount() {
    }

    public YearlyActionCount(final String actionCode) {
        this.actionCode = new StringParameter(actionCode);
    }

    public YearlyActionCount(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    public StringParameter getActionCode() {
        return actionCode;
    }

    public void setActionCode(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return action -> {
            checkArgument(action.getAgent() instanceof Fisher);
            final Fisher fisher = (Fisher) action.getAgent();
            return maybeGetFadManager(fisher)
                .map(fm -> fm.getYearlyActionCounter().getCount(
                    action.getDateTime().getYear(),
                    action.getAgent(),
                    actionCode.getValue()
                ))
                .orElseThrow(() -> new RuntimeException(
                    "FAD manager not found for agent " + fisher
                ));
        };
    }
}
