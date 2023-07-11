package uk.ac.ox.oxfish.regulation.quantities;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;

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
        final String actionCodeValue = actionCode.getValue();
        return action -> {
            checkArgument(action instanceof NumberOfActiveFads.Getter);
            return ((Getter) action).getYearlyActionCount(actionCodeValue);
        };
    }

    public interface Getter {
        long getYearlyActionCount(String actionCode);
    }
}
