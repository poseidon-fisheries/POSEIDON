package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

public class ClearableLogger implements RowProvider {

    private final boolean isEveryStep;
    private final List<String> headers;

    ImmutableList.Builder<List<?>> builder = ImmutableList.builder();

    public ClearableLogger(
        boolean isEveryStep,
        List<String> headers
    ) {
        this.isEveryStep = isEveryStep;
        this.headers = ImmutableList.copyOf(headers);
    }

    public ClearableLogger(boolean isEveryStep, String... headers) {
        this.isEveryStep = isEveryStep;
        this.headers = ImmutableList.copyOf(headers);
    }

    public void addRow(Collection<?> row) {
        builder.add(ImmutableList.copyOf(row));
    }

    public void addRow(Object... row) {
        addRow(ImmutableList.copyOf(row));
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public Iterable<? extends Collection<?>> getRows() {
        final ImmutableList<List<?>> rows = builder.build();
        builder = ImmutableList.builder();
        return rows;
    }

    @Override
    public boolean isEveryStep() {
        return isEveryStep;
    }
}
