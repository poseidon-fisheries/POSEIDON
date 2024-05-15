/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
package uk.ac.ox.poseidon.r.adaptors.datasets.timeseries;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;

import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public abstract class TimeSeriesDatasetFactory implements DatasetFactory {

    @Override
    public Entry<String, Dataset> apply(final Object o) {
        checkArgument(test(o));
        final FishState fishState = (FishState) o;
        return entry(
            getDatasetName(),
            makeDataset(fishState)
        );
    }

    @Override
    public boolean test(final Object o) {
        return FishState.class.isAssignableFrom(o.getClass());
    }

    abstract Dataset makeDataset(FishState fishState);

    abstract TimeSeries<?> getTimeSeries(FishState fishState);

    abstract String getIndexColumnName();

    @Override
    public boolean isAutoRegistered() {
        return true;
    }
}
