package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.core.parameters.DateParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.LocalDate;

public class BetweenDatesFactory implements ComponentFactory<Condition> {

    private DateParameter beginningDate;
    private DateParameter endDate;

    @SuppressWarnings("unused")
    public BetweenDatesFactory() {
    }

    public BetweenDatesFactory(
        final LocalDate beginningDate,
        final LocalDate endDate
    ) {
        this(
            new DateParameter(beginningDate),
            new DateParameter(endDate)
        );
    }

    public BetweenDatesFactory(
        final DateParameter beginningDate,
        final DateParameter endDate
    ) {
        this.beginningDate = beginningDate;
        this.endDate = endDate;
    }

    public DateParameter getBeginningDate() {
        return beginningDate;
    }

    public void setBeginningDate(final DateParameter beginningDate) {
        this.beginningDate = beginningDate;
    }

    public DateParameter getEndDate() {
        return endDate;
    }

    public void setEndDate(final DateParameter endDate) {
        this.endDate = endDate;
    }

    @Override
    public Condition apply(final ModelState ignored) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.BetweenDates(
            beginningDate.getValue(),
            endDate.getValue()
        );
    }
}
