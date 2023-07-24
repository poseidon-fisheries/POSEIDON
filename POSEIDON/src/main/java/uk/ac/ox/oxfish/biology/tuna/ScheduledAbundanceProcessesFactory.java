/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.temporal.ChronoUnit.DAYS;

/**
 * Factory that builds a {@link ScheduledBiologicalProcesses} for {@link AbundanceLocalBiology} by
 * scheduling a chain of processes that handle mortality, aging, recruitment and reallocation.
 */
public class ScheduledAbundanceProcessesFactory
    extends ScheduledBiologicalProcessesFactory<AbundanceLocalBiology> {

    private List<String> biologicalProcessesDates;
    private Map<Species, ? extends RecruitmentProcess> recruitmentProcesses;

    private AlgorithmFactory<AbundanceMortalityProcess> abundanceMortalityProcess;

    /**
     * Empty constructor to allow YAML instantiation.
     */
    @SuppressWarnings("unused")
    public ScheduledAbundanceProcessesFactory() {
    }

    public ScheduledAbundanceProcessesFactory(
        final AlgorithmFactory<Reallocator<AbundanceLocalBiology>> reallocator,
        final Collection<String> biologicalProcessesDates,
        final InputPath mortalityFile
    ) {
        super(reallocator);
        this.biologicalProcessesDates = ImmutableList.copyOf(biologicalProcessesDates);
        this.abundanceMortalityProcess =
            new AbundanceMortalityProcessFromFileFactory(
                mortalityFile,
                ImmutableList.of("natural", "obj_class_1_5", "noa_class_1_5", "longline")
            );
    }

    @SuppressWarnings("unused")
    public AlgorithmFactory<AbundanceMortalityProcess> getAbundanceMortalityProcess() {
        return abundanceMortalityProcess;
    }

    public void setAbundanceMortalityProcess(final AlgorithmFactory<AbundanceMortalityProcess> abundanceMortalityProcess) {
        this.abundanceMortalityProcess = abundanceMortalityProcess;
    }

    @SuppressWarnings("unused")
    public Map<Species, ? extends RecruitmentProcess> getRecruitmentProcesses() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return recruitmentProcesses;
    }

    public void setRecruitmentProcesses(
        final Map<Species, ? extends RecruitmentProcess> recruitmentProcesses
    ) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.recruitmentProcesses = recruitmentProcesses;
    }

    @SuppressWarnings("unused")
    public List<String> getBiologicalProcessesDates() {
        return ImmutableList.copyOf(biologicalProcessesDates);
    }

    @SuppressWarnings("unused")
    public void setBiologicalProcessesDates(final List<String> biologicalProcessesDates) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.biologicalProcessesDates = biologicalProcessesDates;
    }

    @Override
    public ScheduledBiologicalProcesses<AbundanceLocalBiology> apply(final FishState fishState) {
        return new ScheduledBiologicalProcesses<>(
            getReallocator().apply(fishState).getAllocationGrids().getStepMapper(),
            buildSchedule(fishState)
        );
    }

    private Map<Integer, Collection<BiologicalProcess<AbundanceLocalBiology>>> buildSchedule(
        final FishState fishState
    ) {
        final List<LocalDate> dates = biologicalProcessesDates.stream()
            .map(d -> "1970-" + d) // arbitrary non-leap year
            .map(LocalDate::parse)
            .collect(toImmutableList());
        final LocalDate startDate = dates.get(0);
        final Set<Integer> processSteps =
            dates.stream()
                .map(date -> Math.toIntExact(DAYS.between(startDate, date)))
                .collect(toImmutableSet());

        final Reallocator<AbundanceLocalBiology> reallocator = getReallocator().apply(fishState);
        final AllocationGrids<?> grids = reallocator.getAllocationGrids();
        checkState(
            grids.getGrids().keySet().containsAll(processSteps),
            "Biological processes should only be scheduled at steps where we have a grid."
        );

        final ImmutableListMultimap.Builder<Integer, BiologicalProcess<AbundanceLocalBiology>>
            scheduleBuilder = ImmutableListMultimap.builder();

        // Add all our periodical biological processes to the schedule
        final List<BiologicalProcess<AbundanceLocalBiology>> allProcesses =
            ImmutableList.of(
                new AbundanceExtractorProcess(true, true),
                abundanceMortalityProcess.apply(fishState),
                new AbundanceLostRecoveryProcess(),
                new AbundanceAggregatorProcess(),
                new AgingAndRecruitmentProcess(recruitmentProcesses),
                new FadAbundanceExcluderProcess(),
                reallocator
            );

        final List<BiologicalProcess<AbundanceLocalBiology>> reallocationProcesses =
            ImmutableList.of(
                new AbundanceExtractorProcess(false, true),
                reallocator
            );

        grids.getGrids().keySet().forEach(step ->
            scheduleBuilder.putAll(
                step,
                processSteps.contains(step) ? allProcesses : reallocationProcesses
            )
        );

        return scheduleBuilder.build().asMap();
    }
}
