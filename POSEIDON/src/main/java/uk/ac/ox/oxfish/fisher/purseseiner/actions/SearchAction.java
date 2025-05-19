/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.Arriving;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fishing.SetOpportunityDetector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

public class SearchAction extends PurseSeinerAction {

    private final SetOpportunityDetector<?> setOpportunityDetector;
    private final Class<? extends PurseSeinerAction> classForWeighting;

    public SearchAction(
        final Fisher fisher,
        final SetOpportunityDetector<?> setOpportunityDetector,
        final Class<? extends PurseSeinerAction> classForWeighting
    ) {
        super(fisher, fisher.getLocation(), 1);
        this.setOpportunityDetector = setOpportunityDetector;
        this.classForWeighting = classForWeighting;
    }

    @Override
    public ActionResult act(
        final FishState fishState,
        final Fisher fisher,
        final Regulation regulation,
        final double hoursLeft
    ) {
        assert (fisher == getFisher());
        assert (fisher.getLocation() == getLocation());
        setTime(hoursLeft);
        setOpportunityDetector.notifyOfSearch();
        return new ActionResult(new Arriving(), hoursLeft - getDuration());
    }

    /**
     * Fisher preferences for search actions are weighted according to the type of action opportunity
     * they are searching for instead of the action class itself like in the super class.
     */
    @Override
    public Class<? extends PurseSeinerAction> getClassForWeighting() {
        return this.classForWeighting;
    }

    @Override
    public String getCode() {
        return "SEARCH";
    }
}
