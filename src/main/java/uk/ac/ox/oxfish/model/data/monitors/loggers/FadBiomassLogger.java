/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.data.monitors.loggers;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.ImmutableIntArray;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.Collection;
import java.util.List;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.model.FishState;

public class FadBiomassLogger implements RowProvider {

    private final List<String> headers;
    private final FishState fishState;

    public FadBiomassLogger(final FishState fishState) {
        this.fishState = fishState;
        headers = ImmutableList.<String>builder()
            .add("fad_id")
            .add("lon")
            .add("lat")
            .add("step")
            .addAll(fishState.getSpecies().stream().map(Species::getName)::iterator)
            .build();
    }

    @Override
    public boolean isEveryStep() {
        return true;
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Iterable<? extends Collection<?>> getRows() {
        final NauticalMap nauticalMap = fishState.getMap();
        final ImmutableIntArray speciesIndices =
            ImmutableIntArray.copyOf(fishState.getSpecies().stream().mapToInt(Species::getIndex));
        return fishState.getFadMap()
            .allFads()
            .map(fad -> {
                final Coordinate coordinates = nauticalMap.getCoordinates(fad.getLocation());
                return ImmutableList.builder()
                    .add(fad.getId())
                    .add(coordinates.x)
                    .add(coordinates.y)
                    .add(fishState.getStep())
                    .addAll(speciesIndices.stream()
                        .mapToObj(i -> fad.getBiomass()[i])
                        .collect(toImmutableList())
                    )
                    .build();
            })
            .collect(toImmutableList());
    }

}