package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

public class InYearFactory implements ComponentFactory<Condition> {

    private IntegerParameter year;

    @SuppressWarnings("unused")
    public InYearFactory() {
    }

    public InYearFactory(final int year) {
        this(new IntegerParameter(year));
    }

    @SuppressWarnings("WeakerAccess")
    public InYearFactory(final IntegerParameter year) {
        this.year = year;
    }

    public IntegerParameter getYear() {
        return year;
    }

    public void setYear(final IntegerParameter year) {
        this.year = year;
    }

    @Override
    public Condition apply(final ModelState ignored) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.InYear(year.getIntValue());
    }
}
