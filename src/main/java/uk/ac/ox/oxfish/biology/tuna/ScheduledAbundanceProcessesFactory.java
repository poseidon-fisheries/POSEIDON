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

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.time.temporal.ChronoUnit.DAYS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Factory that builds a {@link ScheduledBiologicalProcesses} for {@link AbundanceLocalBiology} by
 * scheduling a chain of processes that handle mortality, aging, recruitment and reallocation.
 */
public class ScheduledAbundanceProcessesFactory
    implements AlgorithmFactory<ScheduledBiologicalProcesses<AbundanceLocalBiology>> {

    private List<String> biologicalProcessesDates;
    private AbundanceReallocator abundanceReallocator;
    private Map<Species, ? extends RecruitmentProcess> recruitmentProcesses;

    /**
     * Empty constructor to allow YAML instantiation.
     */
    @SuppressWarnings("unused")
    public ScheduledAbundanceProcessesFactory() {
    }

    public ScheduledAbundanceProcessesFactory(
        final Collection<String> biologicalProcessesDates
    ) {
        this.biologicalProcessesDates = ImmutableList.copyOf(biologicalProcessesDates);
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

    public void setAbundanceReallocator(final AbundanceReallocator abundanceReallocator) {
        this.abundanceReallocator = abundanceReallocator;
    }

    @Override
    public ScheduledBiologicalProcesses<AbundanceLocalBiology> apply(final FishState fishState) {

        checkNotNull(
            abundanceReallocator,
            "setAbundanceReallocator must be called before using."
        );

        return new ScheduledBiologicalProcesses<>(
            abundanceReallocator.getAllocationGrids().getStepMapper(),
            buildSchedule(),
            new Extractor<>(AbundanceLocalBiology.class, true, true)
        );
    }

    private Map<Integer, Collection<BiologicalProcess<AbundanceLocalBiology>>> buildSchedule() {
        final LocalDate startDate = LocalDate.parse(biologicalProcessesDates.get(0));
        final Set<Integer> processSteps =
            biologicalProcessesDates.stream()
                .map(LocalDate::parse)
                .map(date -> Math.toIntExact(DAYS.between(startDate, date)))
                .collect(toImmutableSet());

        final AllocationGrids<Entry<String, SizeGroup>> grids =
            abundanceReallocator.getAllocationGrids();
        checkState(
            grids.getGrids().keySet().containsAll(processSteps),
            "Biological processes should only be scheduled at steps where we have a grid."
        );

        final ImmutableListMultimap.Builder<Integer, BiologicalProcess<AbundanceLocalBiology>>
            scheduleBuilder = ImmutableListMultimap.builder();

        // Add all our periodical biological processes to the schedule
        final List<BiologicalProcess<AbundanceLocalBiology>> allProcesses =
            ImmutableList.of(
                new AbundanceMortalityProcess(),
                // TODO: add exogenous mortality
                new AbundanceLostRecoveryProcess(),
                new AgingAndRecruitmentProcess(recruitmentProcesses),
                abundanceReallocator
            );

        final List<BiologicalProcess<AbundanceLocalBiology>> reallocationProcesses =
            ImmutableList.of(
                new AbundanceExtractor(false, true),
                abundanceReallocator
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
