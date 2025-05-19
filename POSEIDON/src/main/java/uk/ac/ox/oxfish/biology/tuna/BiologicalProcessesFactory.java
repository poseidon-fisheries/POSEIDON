/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

public abstract class BiologicalProcessesFactory<B extends LocalBiology>
    implements AlgorithmFactory<BiologicalProcesses> {
    private InputPath inputFolder;
    private BiologyInitializerFactory<B> biologyInitializer;
    private RestorerFactory<B> restorer;
    private ScheduledBiologicalProcessesFactory<B> scheduledProcesses;

    public BiologicalProcessesFactory(
        final InputPath inputFolder,
        final BiologyInitializerFactory<B> biologyInitializer,
        final RestorerFactory<B> restorer,
        final ScheduledBiologicalProcessesFactory<B> scheduledProcesses
    ) {
        this.inputFolder = inputFolder;
        this.biologyInitializer = biologyInitializer;
        this.restorer = restorer;
        this.scheduledProcesses = scheduledProcesses;
    }

    public BiologicalProcessesFactory() {

    }

    public ScheduledBiologicalProcessesFactory<B> getScheduledProcesses() {
        return scheduledProcesses;
    }

    public void setScheduledProcesses(final ScheduledBiologicalProcessesFactory<B> scheduledProcesses) {
        this.scheduledProcesses = scheduledProcesses;
    }

    public RestorerFactory<B> getRestorer() {
        return restorer;
    }

    public void setRestorer(final RestorerFactory<B> restorer) {
        this.restorer = restorer;
    }

    public InputPath getInputFolder() {
        return inputFolder;
    }

    public void setInputFolder(final InputPath inputFolder) {
        this.inputFolder = inputFolder;
    }

    @Override
    public BiologicalProcesses apply(final FishState fishState) {
        final BiologyInitializer biologyInitializer = getBiologyInitializer().apply(fishState);
        return new BiologicalProcesses(
            biologyInitializer,
            biologyInitializer.generateGlobal(fishState.getRandom(), fishState),
            ImmutableList.of(
                scheduledProcesses,
                restorer
            )
        );
    }

    public BiologyInitializerFactory<B> getBiologyInitializer() {
        return biologyInitializer;
    }

    public void setBiologyInitializer(final BiologyInitializerFactory<B> biologyInitializer) {
        this.biologyInitializer = biologyInitializer;
    }
}
