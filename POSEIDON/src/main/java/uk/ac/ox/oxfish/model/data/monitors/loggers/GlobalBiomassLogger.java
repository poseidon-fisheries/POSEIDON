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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

public class GlobalBiomassLogger implements RowProvider {

    private final List<String> headers;
    private final FishState fishState;

    public GlobalBiomassLogger(final FishState fishState) {
        this.fishState = fishState;
        headers = ImmutableList.<String>builder()
            .add("source")
            .add("step")
            .addAll(fishState.getSpecies().stream().map(Species::getName)::iterator)
            .build();
    }

    @Override
    public List<String> getHeaders() {
        return headers;
    }

    @Override
    public Iterable<? extends Collection<?>> getRows() {
        return getRows(fishState);
    }

    public static List<List<?>> getRows(final FishState fishState) {
        return ImmutableList.of(
            Stream.concat(
                Stream.of("Ocean", fishState.getStep()),
                getBiomassesStream(fishState, getSeaTileBiologies(fishState))
            ).collect(toImmutableList()),
            Stream.concat(
                Stream.of("FADs", fishState.getStep()),
                getBiomassesStream(fishState, getFadBiologies(fishState))
            ).collect(toImmutableList())
        );
    }

    @NotNull
    public static Stream<Double> getBiomassesStream(
        final FishState fishState,
        final Collection<? extends LocalBiology> biologies
    ) {
        //noinspection UnstableApiUsage
        return fishState.getSpecies()
            .stream()
            .map(species ->
                stream(Optional.ofNullable(biologies))
                    .flatMap(Collection::stream)
                    .mapToDouble(biology -> biology.getBiomass(species))
                    .sum()
            );
    }

    private static ImmutableList<LocalBiology> getSeaTileBiologies(final FishState fishState) {
        return fishState
            .getMap()
            .getAllSeaTilesExcludingLandAsList()
            .stream()
            .map(SeaTile::getBiology)
            .collect(toImmutableList());
    }

    private static ImmutableList<LocalBiology> getFadBiologies(final FishState fishState) {
        return fishState
            .getFadMap()
            .allFads()
            .map(Fad::getBiology)
            .collect(toImmutableList());
    }

    @Override
    public boolean isEveryStep() {
        return true;
    }
}
