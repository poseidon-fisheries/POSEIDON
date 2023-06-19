package uk.ac.ox.oxfish.model.regs;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * Represents a regulation that is only active for part of the year.
 * If startDay > endDay, the regulation is active after start day, through
 * the end of year and before endDay from the beginning of year.
 */
public class TemporaryRegulation extends ConditionalRegulation {

    private final int startDay;
    private final int endDay;

    public TemporaryRegulation(final Regulation delegate, final int startDay, final int endDay) {
        super(delegate);
        this.startDay = startDay;
        this.endDay = endDay;
    }

    @Override
    boolean appliesTo(final Fisher fisher, final int timeStep) {
        final int dayOfTheYear = fisher.grabState().getDayOfTheYear(timeStep);
        return appliesOn(dayOfTheYear);
    }

    boolean appliesOn(final int dayOfTheYear) {
        return startDay <= endDay
            ? dayOfTheYear >= startDay && dayOfTheYear <= endDay
            : dayOfTheYear >= startDay || dayOfTheYear <= endDay;
    }

    @Override
    public Regulation makeCopy() {
        return new TemporaryRegulation(getDelegate().makeCopy(), startDay, endDay);
    }

}
