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

package uk.ac.ox.oxfish.fisher.strategies.destination.fad;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Optional;

public class Route implements Iterator<SeaTile> {

    public static final Route EMPTY = new Route(new LinkedList<>(), null);
    private final Deque<SeaTile> route;
    private final Fisher fisher;

    public Route(Deque<SeaTile> route, Fisher fisher) {
        this.route = route;
        this.fisher = fisher;
    }

    @Override public SeaTile next() {
        return nextDestination().orElseThrow(NoSuchElementException::new);
    }

    private Optional<SeaTile> nextDestination() {
        // if we're at destination and don't want to fish there, move on
        Optional.ofNullable(route.peekFirst())
            .filter(dest -> fisher.getLocation() == dest && !fisher.canAndWantToFishHere())
            .ifPresent(__ -> route.pollFirst());
        return Optional.ofNullable(route.peekFirst());
    }

    @Override public boolean hasNext() {
        return nextDestination().isPresent();
    }

    public boolean isSameAs(Route that) { return this.route.equals(that.route); }

}
