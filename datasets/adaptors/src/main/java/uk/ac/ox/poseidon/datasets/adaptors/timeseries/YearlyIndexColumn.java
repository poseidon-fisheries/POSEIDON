package uk.ac.ox.poseidon.datasets.adaptors.timeseries;

import uk.ac.ox.poseidon.datasets.api.Column;

public class YearlyIndexColumn extends IndexColumn<Long> {
    private final long startYear;

    YearlyIndexColumn(
        final String name,
        final Column<?> indexedColumn,
        final long startYear
    ) {
        super(name, indexedColumn);
        this.startYear = startYear;
    }

    @Override
    protected Long mapIndex(final long index) {
        return startYear + index;
    }
}
