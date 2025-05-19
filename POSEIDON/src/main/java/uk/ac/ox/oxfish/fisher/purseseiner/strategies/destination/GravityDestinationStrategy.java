/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2020-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.destination;

import ec.util.MersenneTwisterFast;
import sim.util.Double2D;
import sim.util.Int2D;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.ActionAttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.AttractionField;
import uk.ac.ox.oxfish.fisher.purseseiner.strategies.fields.PortAttractionField;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.lang.Double.isNaN;
import static java.util.Comparator.comparingDouble;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;
import static uk.ac.ox.oxfish.utility.MasonUtils.bagToStream;

public class GravityDestinationStrategy implements DestinationStrategy {

    private static final Double2D ZERO_VECTOR = new Double2D(0.0, 0.0);

    private final AttractionWeightLoader attractionWeightLoader;
    private final ToDoubleFunction<Fisher> maxTravelTimeLoader;
    private final Predicate<SeaTile> isValidDestination;
    private final Set<AttractionField> attractionFields;
    private Map<AttractionField, Double> attractionWeights;
    private SeaTile destination = null;
    private double maxTravelTime;

    GravityDestinationStrategy(
        final AttractionWeightLoader attractionWeightLoader,
        final ToDoubleFunction<Fisher> maxTravelTimeLoader,
        final Predicate<SeaTile> isValidDestination,
        final Set<AttractionField> attractionFields
    ) {
        this.attractionWeightLoader = attractionWeightLoader;
        this.maxTravelTimeLoader = maxTravelTimeLoader;
        this.isValidDestination = isValidDestination;
        this.attractionFields = attractionFields;
    }

    public Set<AttractionField> getAttractionFields() {
        return attractionFields;
    }

    public double getMaxTravelTime() {
        return maxTravelTime;
    }

    @Override
    public SeaTile chooseDestination(
        final Fisher fisher,
        final MersenneTwisterFast ignored,
        final FishState fishState,
        final Action currentAction
    ) {
        if (destination == null || readyToMoveOn(fisher)) {
            destination = needsToGoBackToPort(fisher)
                ? fisher.getHomePort().getLocation()
                : nextDestination(fisher);
        }
        return destination;
    }

    private boolean readyToMoveOn(final Fisher fisher) {
        return fisher.getLocation() == destination && !fisher.shouldIFish(fisher.grabState());
    }

    private boolean needsToGoBackToPort(final Fisher fisher) {
        final NauticalMap map = fisher.grabState().getMap();
        final SeaTile portLocation = fisher.getHomePort().getLocation();
        final double distanceToPort = map.distance(fisher.getLocation(), portLocation);
        final double travelTimeToPort = distanceToPort / fisher.getBoat().getSpeedInKph();
        return fisher.getHoursAtSea() + travelTimeToPort >= maxTravelTime;
    }

    private SeaTile nextDestination(final Fisher fisher) {
        final SeaTile seaTile = fisher.getLocation();
        final Int2D here = new Int2D(seaTile.getGridX(), seaTile.getGridY());
        final FishState fishState = fisher.grabState();

        return attractionWeights
            .entrySet()
            .stream()
            .filter(entry -> entry.getValue() > 0)
            .map(entry -> entry.getKey().netAttractionHere().multiply(entry.getValue()))
            // TODO: figure out why netAttraction could be NaN
            .filter(v -> !v.equals(ZERO_VECTOR) && !(isNaN(v.x) | isNaN(v.y)))
            .reduce(Double2D::add)
            .map(v -> new Double2D(here.x + 0.5, here.y + 0.5).add(v.normalize()))
            .map(v -> fishState.getMap().getSeaTile((int) v.x, (int) v.y))
            .flatMap(target ->
                (target.isWater() || target.isPortHere()) && isValidDestination.test(target)
                    ? Optional.of(target)
                    : closestNeighbor(fishState.getMap(), seaTile, target)
            )
            .orElse(seaTile);
    }

    private Optional<SeaTile> closestNeighbor(
        final NauticalMap map,
        final SeaTile origin,
        final SeaTile target
    ) {
        final Stream<SeaTile> neighbors = bagToStream(map.getMooreNeighbors(origin, 1));
        return neighbors
            .filter(SeaTile::isWater)
            .filter(isValidDestination)
            .min(comparingDouble(neighbor -> map.distance(neighbor, target)));
    }

    @Override
    public void start(final FishState model, final Fisher fisher) {
        attractionFields.forEach(field -> field.start(model, fisher));
        maxTravelTime = maxTravelTimeLoader.applyAsDouble(fisher);
        initAttractionWeights(fisher);
    }

    private void initAttractionWeights(final Fisher fisher) {
        final Stream<ActionAttractionField> actionAttractionFields = attractionFields.stream()
            .filter(field -> field instanceof ActionAttractionField)
            .map(field -> (ActionAttractionField) field);
        final Map<ActionAttractionField, Double> rawWeights =
            attractionWeightLoader.apply(actionAttractionFields::iterator, fisher);
        final double sum = rawWeights.values().stream().mapToDouble(Double::doubleValue).sum();

        final AttractionField portAttractionField = attractionFields.stream()
            .filter(field -> (field instanceof PortAttractionField))
            .findAny()
            .orElseThrow(() -> new RuntimeException("No port attraction field!"));

        // Give a value of 1.0 to port, and normalize all other fields so that they sum up to 1.0
        attractionWeights = Stream
            .concat(
                rawWeights.entrySet()
                    .stream()
                    .map(entry -> entry(entry.getKey(), entry.getValue() / sum)),
                Stream.of(entry(portAttractionField, 1.0))
            )
            .collect(toImmutableMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @FunctionalInterface
    interface AttractionWeightLoader
        extends BiFunction<Iterable<ActionAttractionField>, Fisher, Map<ActionAttractionField, Double>> {

    }

}
