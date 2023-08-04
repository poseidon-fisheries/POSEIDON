package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;

public class PreviousYearActionCount extends YearlyActionCount {
    public PreviousYearActionCount() {
    }

    public PreviousYearActionCount(final String actionCode) {
        super(actionCode);
    }

    public PreviousYearActionCount(final StringParameter actionCode) {
        super(actionCode);
    }

    @Override
    int getYear(final FishState fishState) {
        return fishState.getCalendarYear() - 1;
    }
}
