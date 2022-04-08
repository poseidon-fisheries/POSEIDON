/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableSetMultimap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction;
import uk.ac.ox.oxfish.model.data.monitors.observers.Observer;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;

public class ActiveActionRegulations implements Observer<PurseSeinerAction> {

    private final ImmutableSetMultimap<Class<? extends PurseSeinerAction>, ActionSpecificRegulation>
        actionSpecificRegulations;
    private final ImmutableSetMultimap<Class<? extends PurseSeinerAction>, YearlyActionLimitRegulation>
        yearlyActionLimitRegulations;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<ActiveFadLimits> activeFadLimits;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<SetLimits> setLimits;

    public ActiveActionRegulations() { this(ImmutableSetMultimap.of()); }

    private ActiveActionRegulations(
        ImmutableSetMultimap<Class<? extends PurseSeinerAction>, ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this.actionSpecificRegulations = actionSpecificRegulations;

        // Store the yearly action limit regulation in a separate collection because
        // it is queried often and the filtering/casting is cumbersome
        this.yearlyActionLimitRegulations =
            actionSpecificRegulations
                .entries()
                .stream()
                .filter(entry -> entry.getValue() instanceof YearlyActionLimitRegulation)
                .collect(toImmutableSetMultimap(
                    Map.Entry::getKey,
                    entry -> (YearlyActionLimitRegulation) entry.getValue()
                ));

        this.activeFadLimits =
            actionSpecificRegulations
                .values()
                .stream()
                .filter(reg -> reg instanceof ActiveFadLimits).map(reg -> (ActiveFadLimits) reg)
                .findFirst();

        this.setLimits =
            actionSpecificRegulations
                .values()
                .stream()
                .filter(reg -> reg instanceof SetLimits).map(reg -> (SetLimits) reg)
                // There are multiple references to set limit in the multimap values,
                // but they should all refer to the same instance
                .findFirst();

    }

    public ActiveActionRegulations(
        Iterable<ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this(stream(actionSpecificRegulations));
    }

    public ActiveActionRegulations(
        Stream<ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this(actionSpecificRegulations
            .collect(flatteningToImmutableSetMultimap(identity(), reg -> reg.getApplicableActions().stream()))
            .inverse());
    }

    public ImmutableSetMultimap<Class<? extends PurseSeinerAction>, YearlyActionLimitRegulation> getYearlyActionLimitRegulations() {
        return yearlyActionLimitRegulations;
    }

    public Optional<ActiveFadLimits> getActiveFadLimits() {
        return activeFadLimits;
    }

    public boolean isForbidden(Class<? extends PurseSeinerAction> purseSeinerAction,
                               Fisher fisher) {
        return actionSpecificRegulations
            .get(purseSeinerAction)
            .stream()
            .anyMatch(reg -> reg.isForbidden(purseSeinerAction,fisher));
    }

    public void observe(PurseSeinerAction purseSeinerAction) {
        actionSpecificRegulations
            .get(purseSeinerAction.getClass())
            .forEach(reg -> reg.observe(purseSeinerAction));
    }

    public boolean anyYearlyLimitedActionRemaining(Fisher fisher) {
        return yearlyActionLimitRegulations.values().isEmpty() ||
            yearlyActionLimitRegulations.values().stream().anyMatch(reg ->
                reg.getNumRemainingActions(fisher) > 0
            );
    }

    public Optional<SetLimits> getSetLimits() {
        return setLimits;
    }


}
