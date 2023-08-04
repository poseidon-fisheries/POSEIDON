package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class SecondLastYearlyFisherValue implements AlgorithmFactory<Quantity> {

    private StringParameter name;

    @SuppressWarnings("unused")
    public SecondLastYearlyFisherValue() {
    }

    public SecondLastYearlyFisherValue(final String name) {
        this(new StringParameter(name));
    }

    @SuppressWarnings("WeakerAccess")
    public SecondLastYearlyFisherValue(final StringParameter name) {
        this.name = name;
    }

    public StringParameter getName() {
        return name;
    }

    public void setName(final StringParameter name) {
        this.name = name;
    }

    @Override
    public Quantity apply(final FishState fishState) {
        return YearlyFisherValue.makeQuantity(name.getValue(), 1);
    }
}
