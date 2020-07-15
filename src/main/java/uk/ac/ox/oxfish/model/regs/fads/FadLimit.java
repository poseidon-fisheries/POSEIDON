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

import java.util.function.Predicate;

public class FadLimit implements ConditionalLimit {

    private final Predicate<Fisher> predicate;
    private final int limit;

    public FadLimit(final Predicate<Fisher> predicate, final int limit) {
        this.predicate = predicate;
        this.limit = limit;
    }

    @Override public int getLimit() {
        return limit;
    }

    @Override public boolean test(final Fisher fisher) {
        return predicate.test(fisher);
    }

}
