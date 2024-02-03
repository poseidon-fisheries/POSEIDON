package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class YearlyGatherer implements ComponentFactory<Quantity> {

    private StringParameter name;

    @SuppressWarnings("unused")
    public YearlyGatherer() {
    }

    public YearlyGatherer(final String name) {
        this.name = new StringParameter(name);
    }

    public YearlyGatherer(final StringParameter name) {
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
        // store name in local variable because we don't
        // want to close over the mutable StringParameter
        final String gathererName = name.getValue();
        return action -> {
            final FishState fishState =
                ((Fisher) action.getAgent()).grabState();
            return fishState
                .getYearlyDataSet()
                .getGatherer(gathererName)
                .apply(fishState);
        };
    }

}
