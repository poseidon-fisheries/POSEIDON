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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.PurseSeinerActionContext;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.agents.api.Agent;
import uk.ac.ox.poseidon.agents.api.YearlyActionCounts;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.YearlyActionCountLimit;

import java.util.List;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.PurseSeinerAction.ACTION_CODES;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class YearlyActionLimitsDepartingStrategy implements DepartingStrategy {

    /**
     * Only leave port if fisher has remaining yearly-limited actions
     */
    @Override
    public boolean shouldFisherLeavePort(final Fisher fisher, final FishState model, final MersenneTwisterFast random) {
        final int year = fisher.grabState().getDate().getYear();
        final FadManager fadManager = getFadManager(fisher);
        final PurseSeinerActionContext actionContext = fadManager.getActionContext();
        return anyYearlyActionsRemaining(fisher, year, fadManager.getRegulations(), actionContext);
    }

    private static boolean anyYearlyActionsRemaining(
        final Agent fisher,
        final int year,
        final Regulations<? super PurseSeinerActionContext> regulations,
        final YearlyActionCounts actionContext
    ) {
        final List<YearlyActionCountLimit> yearlyActionCountLimits =
            regulations
                .asStream()
                .filter(r -> r instanceof YearlyActionCountLimit)
                .map(r -> (YearlyActionCountLimit) r)
                .collect(toImmutableList());
        return ACTION_CODES
            .stream()
            .mapToInt(actionCode ->
                yearlyActionCountLimits
                    .stream()
                    .mapToInt(r -> r.getRemainingActions(year, fisher, actionCode, actionContext))
                    .min()
                    .orElse(Integer.MAX_VALUE)
            )
            .anyMatch(remaining -> remaining > 0);
    }

}
