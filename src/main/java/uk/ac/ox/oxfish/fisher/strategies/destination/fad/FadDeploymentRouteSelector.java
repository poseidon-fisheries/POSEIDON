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

import com.google.common.collect.ArrayTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.Locker;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.ToDoubleBiFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

@SuppressWarnings("UnstableApiUsage")
public class FadDeploymentRouteSelector extends AbstractRouteSelector {

    private final static Locker<NauticalMap, ImmutableList<SeaTile>> possibleRouteTilesLocker = new Locker<>();
    private final ImmutableList<SeaTile> possibleRouteTiles; // will serve as row keys for our ArrayTable of values
    private Map<SeaTile, Double> deploymentLocationValues;
    public FadDeploymentRouteSelector(
        FishState fishState,
        double maxTravelTimeInHours,
        double travelSpeedMultiplier
    ) {
        this(fishState, maxTravelTimeInHours, travelSpeedMultiplier, new HashMap<>());
    }

    private FadDeploymentRouteSelector(
        FishState fishState,
        double maxTravelTimeInHours,
        double travelSpeedMultiplier,
        Map<SeaTile, Double> deploymentLocationValues
    ) {
        super(fishState, maxTravelTimeInHours, travelSpeedMultiplier);
        this.deploymentLocationValues = deploymentLocationValues;
        this.possibleRouteTiles =
            possibleRouteTilesLocker.presentKey(fishState.getMap(), () -> Stream.concat(
                fishState.getMap().getPorts().stream().map(Port::getLocation),
                fishState.getMap().getAllSeaTilesExcludingLandAsList().stream()
            ).collect(toImmutableList()));
    }

    @Override boolean shouldGoToPort(Fisher fisher) { return false; }

    @Override public Set<SeaTile> getPossibleDestinations(Fisher fisher, int timeStep) {
        return getDeploymentLocationValues().keySet();
    }

    public Map<SeaTile, Double> getDeploymentLocationValues() { return deploymentLocationValues; }

    public void setDeploymentLocationValues(Map<SeaTile, Double> deploymentLocationValues) {
        this.deploymentLocationValues = deploymentLocationValues;
    }

    @Override public Stream<SimpleImmutableEntry<Route, Double>> evaluateRoutes(
        Fisher fisher, ImmutableList<PossibleRoute> possibleRoutes, int timeStep
    ) {

        final Table<SeaTile, Integer, Double> seaTileValuesByStep = ArrayTable.create(
            possibleRouteTiles, getTimeStepRange(timeStep, possibleRoutes)
        );

        final ToDoubleBiFunction<SeaTile, Integer> seaTileValueAtStepFunction =
            (seaTile, step) -> {
                final Double cachedValue = seaTileValuesByStep.get(seaTile, step);
                if (cachedValue != null) return cachedValue;
                final double value = canFishAtStep(fisher, seaTile, step)
                    ? getDeploymentLocationValues().getOrDefault(seaTile, 0.0)
                    : 0.0;
                seaTileValuesByStep.put(seaTile, step, value);
                return value;
            };

        return possibleRoutes.stream().map(possibleRoute -> entry(
            possibleRoute.makeRoute(fisher),
            possibleRoute
                .getSteps()
                .stream()
                .mapToDouble(routeStep ->
                    seaTileValueAtStepFunction.applyAsDouble(routeStep.getSeaTile(), routeStep.getTimeStep())
                )
                .sum() - possibleRoute.getCost(fisher)
        ));
    }

    @Override Optional<Deque<SeaTile>> getRoute(
        final Fisher fisher, final SeaTile startingTile, final SeaTile destination
    ) {
        return fisher.isAtPort()
            ? getSimpleRoute(startingTile, destination)
            : getRouteAndBackToPort(fisher, startingTile, destination);
    }

}
