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
package uk.ac.ox.poseidon.r.adaptors.datasets;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;

import java.util.Map;
import java.util.Map.Entry;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

class RowProvidersDataset extends TableMapDataset {

    RowProvidersDataset(
        final FishState fishState,
        final Map<String, ? extends RowProvider> rowProviders
    ) {
        super(
            rowProviders.entrySet().stream().collect(toImmutableMap(
                Entry::getKey,
                entry -> new RowProviderTableAdaptor(entry.getValue())
            ))
        );
        rowProviders.forEach((s, rowProvider) -> {
            if (rowProvider instanceof Startable) {
                fishState.registerStartable((Startable) rowProvider);
            }
        });
    }

}
