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

import com.google.common.collect.ImmutableSortedMap;
import uk.ac.ox.oxfish.fisher.Fisher;

import javax.measure.Quantity;
import javax.measure.quantity.Volume;

import static com.google.common.base.Preconditions.checkArgument;

public class VolumeRelativeLimits implements FisherRelativeLimits {

    private final ImmutableSortedMap<Integer, Integer> limits;

    public VolumeRelativeLimits(ImmutableSortedMap<Integer, Integer> limits) {
        checkArgument(limits.containsKey(0));
        this.limits = limits;
    }

    @Override public int getLimit(Fisher fisher) {
        return fisher.getHold()
            .getVolume()
            .map(this::getLimit)
            .orElseThrow(() -> new IllegalArgumentException(
                "Hold volume needs to be known to get limit for fisher " + fisher
            ));
    }

    public int getLimit(Quantity<Volume> volume) {
        return getLimit(volume.toSystemUnit().getValue().intValue());
    }

    public int getLimit(int volume) {
        checkArgument(volume > 0);
        return limits.floorEntry(volume).getValue();
    }

}
