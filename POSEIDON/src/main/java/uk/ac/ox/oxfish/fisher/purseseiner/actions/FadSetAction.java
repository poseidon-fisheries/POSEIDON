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

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;

import static com.google.common.base.Preconditions.checkArgument;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.FAD;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadSetAction extends AbstractFadSetAction {

    public FadSetAction(
        final Fad fad,
        final Fisher fisher,
        final double duration
    ) {
        super(fad, fisher, duration);
        checkArgument(getFadManager(getFisher()) == fad.getOwner());
    }

    @Override
    public boolean isOwnFad() {
        return true;
    }

    @Override
    public String getCode() {
        return FAD.name();
    }
}
