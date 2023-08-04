package uk.ac.ox.oxfish.regulations.conditions;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DateParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;

import java.time.LocalDate;

public class BetweenDates implements AlgorithmFactory<Condition> {

    private DateParameter beginningDate;
    private DateParameter endDate;

    @SuppressWarnings("unused")
    public BetweenDates() {
    }

    public BetweenDates(final LocalDate beginningDate, final LocalDate endDate) {
        this(
            new DateParameter(beginningDate),
            new DateParameter(endDate)
        );
    }

    public BetweenDates(final DateParameter beginningDate, final DateParameter endDate) {
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
    public Condition apply(final FishState fishState) {
        return new uk.ac.ox.poseidon.regulations.core.BetweenDates(beginningDate.getValue(), endDate.getValue());
    }
}
