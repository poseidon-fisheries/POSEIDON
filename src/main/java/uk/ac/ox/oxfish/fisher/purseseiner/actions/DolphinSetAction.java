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

import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.Fisher;

import static uk.ac.ox.oxfish.fisher.purseseiner.equipment.PurseSeineGear.getPurseSeineGear;

public class DolphinSetAction extends SchoolSetAction {

    public DolphinSetAction(final Fisher fisher) {
        super(
            fisher,
            getPurseSeineGear(fisher).getCatchSamplers().get(DolphinSetAction.class),
            setDuration(fisher, DolphinSetAction.class)
        );
    }

    public DolphinSetAction(final Fisher fisher, final VariableBiomassBasedBiology targetBiology) {
        super(
            fisher,
            targetBiology,
            setDuration(fisher, DolphinSetAction.class)
        );
    }

}
