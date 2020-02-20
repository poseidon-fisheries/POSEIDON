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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.fisher.actions.fads.FadAction;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeFadSet;
import uk.ac.ox.oxfish.fisher.actions.fads.MakeUnassociatedSet;
import uk.ac.ox.oxfish.model.FishState;

public class SetLimits extends YearlyActionLimitRegulation {

    private final ImmutableSet<Class<? extends FadAction>> applicableActions = ImmutableSet.of(
        MakeFadSet.class, MakeUnassociatedSet.class
    );

    public SetLimits(FishState fishState, ImmutableSortedMap<Integer, Integer> limits) {
        super(fishState, new VolumeRelativeLimits(limits));
    }

    @Override public ImmutableSet<Class<? extends FadAction>> getApplicableActions() { return applicableActions; }
}
