package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class YearlyCounter implements AlgorithmFactory<Quantity> {

    private StringParameter name;

    @Override
    public Quantity apply(final FishState fishState) {
        return action ->
            ((Fisher) action.getAgent())
                .grabState()
                .getYearlyCounter()
                .getColumn(name.getValue());
    }

}
