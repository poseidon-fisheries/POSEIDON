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

package uk.ac.ox.oxfish.model.data.webviz.vessels;

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import sim.engine.SimState;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.SteppableJsonBuilder;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static uk.ac.ox.oxfish.model.data.jsonexport.JsonExportUtils.seaTileHeight;
import static uk.ac.ox.oxfish.model.data.jsonexport.JsonExportUtils.seaTileWidth;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.round;

public final class VesselsBuilder implements SteppableJsonBuilder<Vessels> {

    private final ImmutableList.Builder<Timestep> timeStepsBuilder = new ImmutableList.Builder<>();
    private final VesselClassifier vesselClassifier;
    private Stoppable stoppable;

    public VesselsBuilder(final VesselClassifier vesselClassifier) {
        this.vesselClassifier = vesselClassifier;
    }

    @Override public Vessels buildJsonObject(final FishState fishState) {

        final ImmutableList<ImmutableList<Integer>> vesselTypeMap =
            fishState.getFishers().stream()
                .map(fisher -> ImmutableList.of(fisher.getID(), vesselClassifier.applyAsInt(fisher)))
                .collect(toImmutableList());

        return new Vessels(vesselTypeMap, timeStepsBuilder.build());
    }

    @Override public void step(final SimState simState) {

        final FishState fishState = (FishState) simState;
        final MersenneTwisterFast rng = fishState.getRandom();
        final NauticalMap map = fishState.getMap();
        final double seaTileWidth = seaTileWidth(map);
        final double seaTileHeight = seaTileHeight(map);

        final Collection<? extends Collection<Number>> vesselLocations =
            fishState.getFishers().stream()
                .filter(fisher -> !(fisher.isAtPort() && fisher.getHoursAtPort() > 24))
                .map(fisher -> {
                    Coordinate coord = fisher.isAtPort()
                        ? getRoundedCoordinates(fisher, map)
                        : getJiggledCoordinates(fisher, map, seaTileWidth, seaTileHeight, rng);
                    return ImmutableList.<Number>of(fisher.getID(), coord.x, coord.y);
                })
                .collect(toImmutableList());

        timeStepsBuilder.add(new Timestep(fishState.getDay(), vesselLocations));
    }

    private Coordinate getRoundedCoordinates(
        final Fisher fisher,
        final NauticalMap map
    ) {
        final Coordinate coordinates = map.getCoordinates(fisher.getLocation());
        return new Coordinate(round(coordinates.x), round(coordinates.y));
    }

    private Coordinate getJiggledCoordinates(
        final Fisher fisher,
        final NauticalMap map,
        final double seaTileWidth,
        final double seaTileHeight,
        final MersenneTwisterFast rng
    ) {
        final Coordinate coordinates = map.getCoordinates(fisher.getLocation());
        return new Coordinate(
            round(coordinates.x + (rng.nextDouble() - 0.5) * seaTileWidth),
            round(coordinates.y + (rng.nextDouble() - 0.5) * seaTileHeight)
        );
    }

    @Override public Stoppable getStoppable() { return stoppable; }

    @Override public void setStoppable(final Stoppable stoppable) { this.stoppable = stoppable; }

}
