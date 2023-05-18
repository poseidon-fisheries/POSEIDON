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

import uk.ac.ox.oxfish.fisher.Fisher;

import static com.google.common.base.Preconditions.checkArgument;

public final class IATTC {

    private IATTC() {
    }

    public static int capacityClass(Fisher fisher) {
        return capacityClass(fisher.getMaximumHold());
    }

    /**
     * Not used for now, but it might be what we need in the end
     * (See: https://github.com/poseidon-fisheries/tuna/issues/117)
     */
    public static int capacityClass(double carryingCapacityInKg) {
        checkArgument(carryingCapacityInKg > 0, carryingCapacityInKg);
        final long t = Math.round(carryingCapacityInKg / 1000);
        if (t < 46) return 1;
        else if (t <= 91) return 2;
        else if (t <= 181) return 3;
        else if (t <= 272) return 4;
        else if (t <= 363) return 5;
        else return 6;
    }

}
