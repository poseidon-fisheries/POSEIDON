package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class LastYearlyFisherValue implements ComponentFactory<Quantity> {

    private StringParameter name;

    @SuppressWarnings("unused")
    public LastYearlyFisherValue() {
    }

    public LastYearlyFisherValue(final String name) {
        this(new StringParameter(name));
    }

    @SuppressWarnings("WeakerAccess")
    public LastYearlyFisherValue(final StringParameter name) {
        this.name = name;
    }

    public StringParameter getName() {
        return name;
    }

    public void setName(final StringParameter name) {
        this.name = name;
    }

    @Override
    public Quantity apply(final ModelState ignored) {
        return YearlyFisherValue.makeQuantity(name.getValue(), 0);
    }
}
