package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class AbundanceProcessesFactory
    extends BiologicalProcessesFactory<Entry<String, SizeGroup>, AbundanceLocalBiology> {

    private RecruitmentProcessesFactory recruitmentProcessesFactory;
    private WeightGroupsFactory weightGroupsFactory;

    public AbundanceProcessesFactory(
        final InputPath inputFolder,
        final SpeciesCodesFromFileFactory speciesCodesSupplier
    ) {
        super(
            inputFolder,
            speciesCodesSupplier,
            new AbundanceInitializerFactory(
                inputFolder.path("bins.csv"),
                speciesCodesSupplier
            ),
            new AbundanceReallocatorFactory(
                inputFolder.path("grids.csv"),
                365,
                speciesCodesSupplier
            ),
            new AbundanceRestorerFactory(
                ImmutableMap.of(0, 365)
            ),
            new ScheduledAbundanceProcessesFactory(
                speciesCodesSupplier,
                ImmutableList.of("2017-01-01", "2017-04-01", "2017-07-01", "2017-10-01"),
                inputFolder.path("mortality.csv")
            )
        );
        this.recruitmentProcessesFactory =
            new RecruitmentProcessesFactory(
                speciesCodesSupplier,
                inputFolder.path("recruitment_parameters.csv")
            );
        this.weightGroupsFactory =
            new WeightGroupsFactory(
                Stream.of("Bigeye tuna", "Skipjack tuna", "Yellowfin tuna").collect(
                    toImmutableMap(identity(), __ -> ImmutableList.of("small", "medium", "large"))
                ),
                ImmutableMap.of(
                    "Bigeye tuna", ImmutableList.of(2.5, 15.0),
                    // use the last two bins of SKJ as "medium" and "large"
                    "Skipjack tuna", ImmutableList.of(2.5, 11.5019),
                    "Yellowfin tuna", ImmutableList.of(2.5, 15.0)
                )
            );
    }

    @SuppressWarnings("unused")
    public AbundanceProcessesFactory() {
    }

    @Override
    public Processes initProcesses(final NauticalMap nauticalMap, final FishState fishState) {
        ((AbundanceInitializerFactory) getBiologyInitializerFactory())
            .assignWeightGroupsPerSpecies(weightGroupsFactory.apply(fishState));
        final Processes processes = super.initProcesses(nauticalMap, fishState);
        recruitmentProcessesFactory.setGlobalBiology(processes.globalBiology);
        ((ScheduledAbundanceProcessesFactory) getScheduledProcessesFactory())
            .setRecruitmentProcesses(recruitmentProcessesFactory.apply(fishState));
        return processes;
    }

    public WeightGroupsFactory getWeightGroupsFactory() {
        return weightGroupsFactory;
    }

    public void setWeightGroupsFactory(final WeightGroupsFactory weightGroupsFactory) {
        this.weightGroupsFactory = weightGroupsFactory;
    }

    public RecruitmentProcessesFactory getRecruitmentProcessesFactory() {
        return recruitmentProcessesFactory;
    }

    public void setRecruitmentProcessesFactory(final RecruitmentProcessesFactory recruitmentProcessesFactory) {
        this.recruitmentProcessesFactory = recruitmentProcessesFactory;
    }
}
