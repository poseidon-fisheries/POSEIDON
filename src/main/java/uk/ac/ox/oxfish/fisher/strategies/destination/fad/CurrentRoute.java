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

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Deque;
import java.util.Optional;

public class CurrentRoute {

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Deque<SeaTile>> route = Optional.empty();

    public Optional<SeaTile> nextDestination(Fisher fisher) {
        return route.map(deque -> Optional
            // look at the head of the route
            .ofNullable(deque.peekFirst())
            // keep it if we're not there yet, or if we're there but still want to fish
            .filter(destination -> fisher.getLocation() != destination || fisher.canAndWantToFishHere())
            .orElseGet(() -> {
                // otherwise, remove it from the return and return the next tile if there is one
                deque.removeFirst();
                return deque.peekFirst();
            })
        );
    }

    public void selectNewRoute(RouteSelector routeSelector, Fisher fisher, int timeStep, MersenneTwisterFast rng) {
        route = routeSelector.selectRoute(fisher, timeStep, rng);
    }
}
