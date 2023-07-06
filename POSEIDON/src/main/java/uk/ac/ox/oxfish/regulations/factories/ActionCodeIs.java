package uk.ac.ox.oxfish.regulations.factories;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.agents.api.Action;

import java.util.function.Predicate;

public class ActionCodeIs implements AlgorithmFactory<Predicate<Action>> {
    private StringParameter actionCode;

    public ActionCodeIs() {
    }

    public ActionCodeIs(final String actionCode) {
        this(new StringParameter(actionCode));
    }

    public ActionCodeIs(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    public StringParameter getActionCode() {
        return actionCode;
    }

    public void setActionCode(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    @Override
    public Predicate<Action> apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.ActionCodeIs(actionCode.getValue());
    }
}
