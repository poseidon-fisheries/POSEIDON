package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.poseidon.datasets.api.Column;

import java.time.LocalDate;

public class DailyIndexColumn extends IndexColumn<LocalDate> {
    private final LocalDate startDate;

    DailyIndexColumn(
        final String name,
        final Column<?> indexedColumn,
        final LocalDate startDate
    ) {
        super(name, indexedColumn);
        this.startDate = startDate;
    }

    @Override
    protected LocalDate mapIndex(final long index) {
        return startDate.plusDays(index);
    }
}
