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
import uk.ac.ox.oxfish.fisher.Fisher;

import java.util.List;

public class ConditionalFisherRelativeLimits implements FisherRelativeLimits {

    private final List<ConditionalLimit> limits;

    ConditionalFisherRelativeLimits(Iterable<? extends ConditionalLimit> limits) {
        this.limits = ImmutableList.copyOf(limits);
    }

    @Override public int getLimit(Fisher fisher) {
        return limits.stream()
            .filter(limit -> limit.test(fisher))
            .findFirst()
            .map(ConditionalLimit::getLimit)
            .orElseThrow(() -> new IllegalArgumentException("No limit applies to fisher " + fisher));
    }

}
