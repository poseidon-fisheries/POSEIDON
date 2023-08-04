package uk.ac.ox.oxfish.regulations.quantities;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.parameters.StringParameter;

public class CurrentYearActionCount extends YearlyActionCount {
    @SuppressWarnings("unused")
    public CurrentYearActionCount() {
    }

    public CurrentYearActionCount(final String actionCode) {
        super(actionCode);
    }

    @SuppressWarnings("unused")
    public CurrentYearActionCount(final StringParameter actionCode) {
        super(actionCode);
    }

    @Override
    int getYear(final FishState fishState) {
        return fishState.getCalendarYear();
    }
}
