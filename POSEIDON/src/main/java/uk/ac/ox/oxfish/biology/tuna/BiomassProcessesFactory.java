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
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.event.BiomassDrivenTimeSeriesExogenousCatchesFactory;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

public class BiomassProcessesFactory extends BiologicalProcessesFactory<BiomassLocalBiology> {
    private BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory;

    public BiomassProcessesFactory() {
    }

    public BiomassProcessesFactory(
        final InputPath inputFolder,
        final BiologyInitializerFactory<BiomassLocalBiology> biologyInitializer,
        final RestorerFactory<BiomassLocalBiology> restorer,
        final ScheduledBiologicalProcessesFactory<BiomassLocalBiology> scheduledProcesses,
        final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory
    ) {
        super(inputFolder, biologyInitializer, restorer, scheduledProcesses);
        this.exogenousCatchesFactory = exogenousCatchesFactory;
    }

    public static BiomassProcessesFactory create(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier,
        final IntegerParameter targetYear,
        final ComponentFactory<MapExtent> mapExtentFactory

    ) {
        final BiomassReallocatorFactory reallocator = new BiomassReallocatorFactory(
            inputFolder.path("biomass_distributions.csv"),
            new IntegerParameter(365),
            mapExtentFactory
        );
        return new BiomassProcessesFactory(
            inputFolder,
            new BiomassInitializerFactory(
                reallocator,
                speciesCodesSupplier,
                inputFolder.path("schaefer_params.csv")
            ),
            new BiomassRestorerFactory(
                reallocator,
                ImmutableMap.of(0, 364)
            ),
            new ScheduledBiomassProcessesFactory(reallocator),
            new BiomassDrivenTimeSeriesExogenousCatchesFactory(
                speciesCodesSupplier,
                inputFolder.path("exogenous_catches.csv"),
                targetYear,
                true
            )
        );
    }

    @Override
    public BiologicalProcesses apply(final FishState fishState) {
        final BiologicalProcesses biologicalProcesses = super.apply(fishState);
        return new BiologicalProcesses(
            biologicalProcesses.getBiologyInitializer(),
            biologicalProcesses.getGlobalBiology(),
            ImmutableList.<AlgorithmFactory<? extends Startable>>builder()
                .addAll(biologicalProcesses.getStartableFactories())
                .add(exogenousCatchesFactory)
                .build()
        );
    }

    public BiomassDrivenTimeSeriesExogenousCatchesFactory getExogenousCatchesFactory() {
        return exogenousCatchesFactory;
    }

    @SuppressWarnings("unused")
    public void setExogenousCatchesFactory(final BiomassDrivenTimeSeriesExogenousCatchesFactory exogenousCatchesFactory) {
        this.exogenousCatchesFactory = exogenousCatchesFactory;
    }
}
