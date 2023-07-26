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
import uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class DepartIfAnyActionPermitted implements DepartingStrategy {

    /**
     * Only leave port if fisher has remaining yearly-limited actions
     */
    @Override
    public boolean shouldFisherLeavePort(final Fisher fisher, final FishState model, final MersenneTwisterFast random) {
        final Regulation regulation = getFadManager(fisher).getRegulation();
        return ActionClass.CODES.stream()
            .map(code -> new FadManager.DummyAction(code, fisher))
            .anyMatch(regulation::isPermitted);
    }

}
