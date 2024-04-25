/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) -2024 CoHESyS Lab cohesys.lab@gmail.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package uk.ac.ox.oxfish.model.data.monitors.loggers;

import com.google.common.collect.ImmutableList;

import java.util.Collection;
import java.util.List;

public class ClearableLogger implements RowProvider {

    private final boolean isEveryStep;
    private final List<String> headers;

    ImmutableList.Builder<List<?>> builder = ImmutableList.builder();

    public ClearableLogger(
        final boolean isEveryStep,
        final List<String> headers
    ) {
        this.isEveryStep = isEveryStep;
        this.headers = ImmutableList.copyOf(headers);
    }

    public ClearableLogger(
        final boolean isEveryStep,
        final String... headers
    ) {
        this.isEveryStep = isEveryStep;
        this.headers = ImmutableList.copyOf(headers);
    }

    public void addRow(final Object... row) {
        addRow(ImmutableList.copyOf(row));
    }

    public void addRow(final Collection<?> row) {
        builder.add(ImmutableList.copyOf(row));
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        final ImmutableList<List<?>> rows = builder.build();
        builder = ImmutableList.builder();
        return rows;
    }

    @Override
    public boolean isEveryStep() {
        return isEveryStep;
    }
}
