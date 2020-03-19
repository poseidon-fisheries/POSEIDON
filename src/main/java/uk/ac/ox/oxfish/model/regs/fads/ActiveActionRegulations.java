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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSetMultimap;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static java.util.function.Function.identity;

public class ActiveActionRegulations {
    private ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> actionSpecificRegulations;

    public ActiveActionRegulations() { this(ImmutableSetMultimap.of()); }

    public ActiveActionRegulations(
        ImmutableSetMultimap<Class<? extends FadAction>, ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this.actionSpecificRegulations = actionSpecificRegulations;
    }

    public ActiveActionRegulations(
        Stream<ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this(actionSpecificRegulations
            .collect(flatteningToImmutableSetMultimap(identity(), reg -> reg.getApplicableActions().stream()))
            .inverse());
    }

    public boolean isAllowed(FadAction fadAction) {
        return regulationStream(fadAction).allMatch(reg -> reg.isAllowed(fadAction));
    }

    public Stream<ActionSpecificRegulation> regulationStream(FadAction fadAction) {
        return regulationStream(fadAction.getClass());
    }

    public Stream<ActionSpecificRegulation> regulationStream(Class<? extends FadAction> fadActionClass) {
        return actionSpecificRegulations.get(fadActionClass).stream();
    }

    public void reactToAction(FadAction fadAction) {
        regulationStream(fadAction).forEach(reg -> reg.reactToAction(fadAction));
    }

    public boolean anyYearlyLimitedActionRemaining(Fisher fisher) {
        final ImmutableList<YearlyActionLimitRegulation> yearlyActionLimitRegulations = getYearlyActionLimitRegulations();
        return yearlyActionLimitRegulations.isEmpty() ||
            yearlyActionLimitRegulations.stream().anyMatch(reg ->
                reg.getNumRemainingActions(fisher) > 0
            );
    }

    public ImmutableList<YearlyActionLimitRegulation> getYearlyActionLimitRegulations() {
        return actionSpecificRegulations
            .values()
            .stream()
            .filter(reg -> reg instanceof YearlyActionLimitRegulation)
            .map(reg -> (YearlyActionLimitRegulation) reg)
            .collect(toImmutableList());
    }

}
