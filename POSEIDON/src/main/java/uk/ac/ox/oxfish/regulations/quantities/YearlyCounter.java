package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class YearlyCounter implements AlgorithmFactory<Quantity> {

    private StringParameter name;

    @SuppressWarnings("unused")
    public YearlyCounter() {
    }

    public YearlyCounter(final String name) {
        this.name = new StringParameter(name);
    }

    public YearlyCounter(final StringParameter name) {
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
        // store name in local variable because we don't
        // want to close over the mutable StringParameter
        final String columnName = name.getValue();
        return action ->
            ((Fisher) action.getAgent())
                .grabState()
                .getYearlyCounter()
                .getColumn(columnName);
    }

}
