package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcesses;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.geography.MapExtent;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

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
        final AlgorithmFactory<MapExtent> mapExtent,
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
                ImmutableMap.of(0, 365)
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
