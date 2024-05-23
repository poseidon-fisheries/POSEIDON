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
package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcesses;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class AbundanceProcessesFactory
    extends BiologicalProcessesFactory<AbundanceLocalBiology> {

    private WeightGroupsFactory weightGroups;

    @SuppressWarnings("unused")
    public AbundanceProcessesFactory() {
    }

    public AbundanceProcessesFactory(
        final InputPath inputFolder,
        final BiologyInitializerFactory<AbundanceLocalBiology> biologyInitializer,
        final RestorerFactory<AbundanceLocalBiology> restorer,
        final ScheduledBiologicalProcessesFactory<AbundanceLocalBiology> scheduledProcesses
    ) {
        super(inputFolder, biologyInitializer, restorer, scheduledProcesses);
        this.weightGroups =
            new WeightGroupsFactory(
                Stream.of("Bigeye tuna", "Skipjack tuna", "Yellowfin tuna").collect(
                    toImmutableMap(identity(), __ -> ImmutableList.of("small", "medium", "large"))
                ),
                ImmutableMap.of(
                    "Bigeye tuna", ImmutableList.of(2.5, 15.0),
                    "Skipjack tuna", ImmutableList.of(2.5, 3.0),
                    "Yellowfin tuna", ImmutableList.of(2.5, 15.0)
                )
            );
    }

    public static AbundanceProcessesFactory create(
        final InputPath inputFolder,
        final AlgorithmFactory<SpeciesCodes> speciesCodesSupplier,
        final ComponentFactory<MapExtent> mapExtent,
        final AlgorithmFactory<RecruitmentProcesses> recruitmentProcesses
    ) {
        final AbundanceReallocatorFactory reallocator =
            new AbundanceReallocatorFactory(
                inputFolder.path("species_distribution_maps_2021_to_2023.csv"),
                new IntegerParameter(365 * 3),
                mapExtent
            );
        return new AbundanceProcessesFactory(
            inputFolder,
            new AbundanceInitializerFactory(
                reallocator,
                inputFolder.path("bins_2022.csv"),
                speciesCodesSupplier
            ),
            new AbundanceRestorerFactory(
                reallocator,
                ImmutableMap.of("0", 365)
            ),
            new ScheduledAbundanceProcessesFactory(
                recruitmentProcesses,
                reallocator,
                ImmutableList.of("01-01", "04-01", "07-01", "10-01"),
                inputFolder.path("mortality_2022.csv")
            )
        );
    }

    @Override
    public BiologicalProcesses apply(final FishState fishState) {
        ((AbundanceInitializerFactory) getBiologyInitializer())
            .assignWeightGroupsPerSpecies(weightGroups.apply(fishState));
        return super.apply(fishState);
    }

    public WeightGroupsFactory getWeightGroups() {
        return weightGroups;
    }

    public void setWeightGroups(final WeightGroupsFactory weightGroups) {
        this.weightGroups = weightGroups;
    }

}
