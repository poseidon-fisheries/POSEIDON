package uk.ac.ox.poseidon.common.core.parameters;

import java.time.LocalDate;

public class DateParameter extends FixedParameter<LocalDate> {
    public DateParameter(final LocalDate value) {
        super(value);
    }
}
