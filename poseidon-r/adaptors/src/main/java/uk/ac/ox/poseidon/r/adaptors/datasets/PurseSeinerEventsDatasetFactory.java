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

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineActionsLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.PurseSeineTripLogger;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;
import uk.ac.ox.poseidon.epo.scenarios.EpoScenario;

import java.util.Map;
import java.util.function.Function;

@AutoService(DatasetFactory.class)
public class PurseSeinerEventsDatasetFactory extends RowProviderDatasetFactory {
    @Override
    Map<String, Function<FishState, RowProvider>> getRowProviderFactories() {
        return ImmutableMap.of(
            "Actions", PurseSeineActionsLogger::new,
            "Trips", PurseSeineTripLogger::new
        );
    }

    @Override
    public String getDatasetName() {
        return "Purse-seiner events";
    }

    @Override
    public boolean test(final Object o) {
        return o instanceof FishState &&
            ((FishState) o).getScenario() instanceof EpoScenario;
    }
}
