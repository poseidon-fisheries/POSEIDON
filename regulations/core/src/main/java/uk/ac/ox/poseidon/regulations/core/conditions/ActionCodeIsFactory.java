package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class ActionCodeIsFactory implements ComponentFactory<Condition> {
    private StringParameter actionCode;

    public ActionCodeIsFactory() {
    }

    public ActionCodeIsFactory(final String actionCode) {
        this(new StringParameter(actionCode));
    }

    public ActionCodeIsFactory(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    public StringParameter getActionCode() {
        return actionCode;
    }

    public void setActionCode(final StringParameter actionCode) {
        this.actionCode = actionCode;
    }

    @Override
    public Condition apply(final ModelState ignored) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.ActionCodeIs(actionCode.getValue());
    }
}
