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

import com.google.common.collect.Iterators;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.Moving;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Iterator;

public class FadDestinationStrategy implements DestinationStrategy {

    private final FadDeploymentRouteSelector fadDeploymentRouteSelector;
    private final FadSettingRouteSelector fadSettingRouteSelector;
    private final Iterator<RouteSelector> routeSelectors;

    private Route currentRoute = Route.EMPTY;

    public FadDestinationStrategy(
        NauticalMap map,
        FadDeploymentRouteSelector fadDeploymentRouteSelector,
        FadSettingRouteSelector fadSettingRouteSelector
    ) {
        this.fadDeploymentRouteSelector = fadDeploymentRouteSelector;
        this.fadSettingRouteSelector = fadSettingRouteSelector;
        this.routeSelectors = Iterators.cycle(
            fadDeploymentRouteSelector,
            fadSettingRouteSelector,
            fadDeploymentRouteSelector,
            new RouteToPortSelector(map)
        );
    }

    public FadDeploymentRouteSelector getFadDeploymentRouteSelector() { return fadDeploymentRouteSelector; }

    public FadSettingRouteSelector getFadSettingRouteSelector() { return fadSettingRouteSelector; }

    @Override
    public SeaTile chooseDestination(Fisher fisher, MersenneTwisterFast rng, FishState model, Action currentAction) {
        // don't change destination while we're moving
        if (currentAction instanceof Moving) return fisher.getDestination();
        // if we don't have a current destination, loop through selectors until we find one
        while (!currentRoute.hasNext())
            currentRoute = routeSelectors.next()
                .selectRoute(fisher, model.getStep(), rng)
                .orElse(Route.EMPTY);
        return currentRoute.next();
    }

}
