/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
package uk.ac.ox.poseidon.simulations.adaptors;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.common.core.Services;
import uk.ac.ox.poseidon.datasets.api.Dataset;
import uk.ac.ox.poseidon.datasets.api.DatasetFactory;
import uk.ac.ox.poseidon.simulations.api.Simulation;

import java.util.Map;
import java.util.Map.Entry;

import static java.util.stream.Collectors.toMap;

public class FishStateAdaptor implements Simulation {

    private final FishState fishState;
    private final Map<String, Dataset> datasets;

    FishStateAdaptor(final FishState fishState) {
        this.fishState = fishState;
        this.datasets = Services
            .loadAll(
                DatasetFactory.class,
                datasetFactory -> datasetFactory.isAutoRegistered() &&
                    datasetFactory.test(fishState)
            )
            .stream()
            .map(datasetFactory -> datasetFactory.apply(fishState))
            .collect(toMap(Entry::getKey, Entry::getValue));
    }

    @Override
    public int getStep() {
        return fishState.getStep();
    }

    @Override
    public String getId() {
        return fishState.getUniqueID();
    }

    @Override
    public void step() {
        if (!fishState.isStarted()) {
            fishState.start();
        }
        fishState.schedule.step(fishState);
    }

    @Override
    public Map<String, Dataset> getDatasets() {
        return datasets;
    }
}
