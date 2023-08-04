package uk.ac.ox.oxfish.regulations.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class InYear implements AlgorithmFactory<Condition> {

    private IntegerParameter year;

    @SuppressWarnings("unused")
    public InYear() {
    }

    public InYear(final int year) {
        this(new IntegerParameter(year));
    }

    @SuppressWarnings("WeakerAccess")
    public InYear(final IntegerParameter year) {
        this.year = year;
    }

    public IntegerParameter getYear() {
        return year;
    }

    public void setYear(final IntegerParameter year) {
        this.year = year;
    }

    @Override
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.InYear(year.getIntValue());
    }
}
