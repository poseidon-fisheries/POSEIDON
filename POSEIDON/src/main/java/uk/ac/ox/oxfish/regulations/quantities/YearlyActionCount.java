package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import static com.google.common.base.Preconditions.checkArgument;

public abstract class YearlyActionCount implements AlgorithmFactory<Quantity> {

    private StringParameter actionCode;

    public YearlyActionCount() {
    }

    public YearlyActionCount(final String actionCode) {
        this(new StringParameter(actionCode));
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
            checkArgument(action instanceof Getter);
            return ((Getter) action).getYearlyActionCount(getYear(fishState), actionCodeValue);
        };
    }

    abstract int getYear(final FishState fishState);

    public interface Getter {
        long getYearlyActionCount(int year, String actionCode);
    }
}
