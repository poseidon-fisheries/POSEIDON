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
import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.time.temporal.ChronoUnit.DAYS;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableMap;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.RecruitmentProcess;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * TODO.
 */
public class ScheduledAbundanceProcessesFactory
    implements AlgorithmFactory<ScheduledAbundanceProcesses> {

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

    public void setRecruitmentProcesses(
        final Map<Species, ? extends RecruitmentProcess> recruitmentProcesses
    ) {
        this.recruitmentProcesses = ImmutableMap.copyOf(recruitmentProcesses);
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
    public ScheduledAbundanceProcesses apply(final FishState fishState) {

        checkNotNull(
            abundanceReallocator,
            "setAbundanceReallocator must be called before using."
        );

        final AbundanceAggregator aggregator = new AbundanceAggregator();
        return new ScheduledAbundanceProcesses(
            aggregator,
            abundanceReallocator.getAllocationGrids().getStepMapper(),
            buildSchedule(aggregator)
        );
    }

    private Map<Integer, Collection<BiologicalProcess<AbundanceLocalBiology>>> buildSchedule(
        final AbundanceAggregator aggregator
    ) {
        final LocalDate startDate = LocalDate.parse(biologicalProcessesDates.get(0));
        final ImmutableList<Integer> processSteps =
            biologicalProcessesDates.stream()
                .map(LocalDate::parse)
                .map(date -> Math.toIntExact(DAYS.between(startDate, date)))
                .collect(toImmutableList());

        final AllocationGrids<Entry<String, SizeGroup>> grids =
            abundanceReallocator.getAllocationGrids();
        checkState(
            grids.getGrids().keySet().containsAll(processSteps),
            "Biological processes should only be scheduled at steps where we have a grid."
        );

        final ImmutableListMultimap.Builder<Integer, BiologicalProcess<AbundanceLocalBiology>>
            scheduleBuilder = ImmutableListMultimap.builder();

        // Add all our periodical biological processes to the schedule
        final List<BiologicalProcess<AbundanceLocalBiology>> periodicalProcesses =
            ImmutableList.of(
                new MortalityProcess(),
                new AgingAndRecruitmentProcess(recruitmentProcesses),
                new FadAbundanceExcluder(aggregator)
            );

        processSteps.forEach(step ->
            scheduleBuilder.putAll(step, periodicalProcesses)
        );

        // Schedule a reallocator for each time step we have a grid.
        // It should always be the last process for that time step.

        grids.getGrids().keySet().forEach(step ->
            scheduleBuilder.put(step, abundanceReallocator)
        );

        return scheduleBuilder.build().asMap();
    }
}
