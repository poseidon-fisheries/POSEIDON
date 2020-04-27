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

package uk.ac.ox.oxfish.model.data.webviz.fads;

import com.google.common.collect.ImmutableList;
import sim.engine.SimState;
import sim.field.geo.GeomGridField;
import sim.util.Double2D;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.webviz.SteppableJsonBuilder;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.round;

public final class FadsBuilder implements SteppableJsonBuilder<Fads> {

    private final ImmutableList.Builder<Timestep> timestepsBuilder = new ImmutableList.Builder<>();

    @Override public Fads buildJsonObject(final FishState fishState) {
        return new Fads(timestepsBuilder.build());
    }

    @Override public void step(final SimState simState) {
        final FishState fishState = (FishState) simState;
        final Timestep timestep = Optional.ofNullable(fishState.getFadMap())
            .map(fadMap -> new Timestep(fishState.getDay(), fadLocations(fishState.getMap(), fadMap)))
            .orElseGet(() -> {
                // Need to add at least 1000 dummy FADs to satisfy the web app
                // https://github.com/poseidon-fisheries/poseidon-webviz/issues/34 and
                // https://github.com/poseidon-fisheries/poseidon-webviz/issues/35
                return new Timestep(
                    fishState.getDay(),
                    Stream.generate(() -> new double[]{0, 0}).limit(1000).collect(toList())
                );
            });
        timestepsBuilder.add(timestep);
    }

    private Collection<double[]> fadLocations(final NauticalMap nauticalMap, final FadMap fadMap) {
        final GeomGridField field = nauticalMap.getRasterBathymetry();
        final double minLon = field.MBR.getMinX();
        final double minLat = field.MBR.getMinY();
        final double maxLon = field.MBR.getMaxX();
        final double maxLat = field.MBR.getMaxY();
        final double maxX = field.getGridWidth();
        final double maxY = field.getGridHeight();
        @SuppressWarnings("unchecked") final Collection<Double2D> locations =
            fadMap.getField().doubleLocationHash.values();
        return locations.stream()
            .map(loc -> new double[]{
                round(minLon + (loc.x / maxX) * (maxLon - minLon)),
                round(maxLat - (loc.y / maxY) * (maxLat - minLat))
            })
            .collect(toImmutableList());
    }

}