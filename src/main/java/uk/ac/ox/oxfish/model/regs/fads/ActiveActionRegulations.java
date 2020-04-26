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
import uk.ac.ox.oxfish.fisher.actions.purseseiner.PurseSeinerAction;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.Function.identity;

public class ActiveActionRegulations {

    private ImmutableSetMultimap<Class<? extends PurseSeinerAction>, ActionSpecificRegulation>
        actionSpecificRegulations;

    public ActiveActionRegulations() { this(ImmutableSetMultimap.of()); }

    private ActiveActionRegulations(
        ImmutableSetMultimap<Class<? extends PurseSeinerAction>, ActionSpecificRegulation> actionSpecificRegulations
    ) {
        this.actionSpecificRegulations = actionSpecificRegulations;
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

    public boolean isForbidden(PurseSeinerAction purseSeinerAction) {
        return regulationStream(purseSeinerAction).anyMatch(reg -> reg.isForbidden(purseSeinerAction));
    }

    private Stream<ActionSpecificRegulation> regulationStream(PurseSeinerAction purseSeinerAction) {
        return regulationStream(purseSeinerAction.getClass());
    }

    public Stream<ActionSpecificRegulation> regulationStream(Class<? extends PurseSeinerAction> actionClass) {
        return actionSpecificRegulations.get(actionClass).stream();
    }

    public void reactToAction(PurseSeinerAction purseSeinerAction) {
        regulationStream(purseSeinerAction).forEach(reg -> reg.reactToAction(purseSeinerAction));
    }

    public boolean anyYearlyLimitedActionRemaining(Fisher fisher) {
        final ImmutableList<YearlyActionLimitRegulation> yearlyActionLimitRegulations =
            getYearlyActionLimitRegulations();
        return yearlyActionLimitRegulations.isEmpty() ||
            yearlyActionLimitRegulations.stream().anyMatch(reg ->
                reg.getNumRemainingActions(fisher) > 0
            );
    }

    private ImmutableList<YearlyActionLimitRegulation> getYearlyActionLimitRegulations() {
        return actionSpecificRegulations
            .values()
            .stream()
            .filter(reg -> reg instanceof YearlyActionLimitRegulation)
            .map(reg -> (YearlyActionLimitRegulation) reg)
            .collect(toImmutableList());
    }

}
