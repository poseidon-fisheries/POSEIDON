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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;

abstract class Aggregator<A, B extends LocalBiology> {

    private final Class<B> localBiologyClass;

    protected Aggregator(final Class<B> localBiologyClass) {
        this.localBiologyClass = localBiologyClass;
    }

    public Class<B> getLocalBiologyClass() {
        return localBiologyClass;
    }

    @SuppressWarnings("UnstableApiUsage")
    public Map<Species, A> aggregate(
        final GlobalBiology globalBiology,
        @Nullable final NauticalMap nauticalMap,
        @Nullable final FadMap fadMap
    ) {
        return aggregate(globalBiology, getLocalBiologies(nauticalMap, fadMap));
    }

    Collection<B> getLocalBiologies(
        @Nullable final NauticalMap nauticalMap,
        @Nullable final FadMap fadMap
    ) {
        final Stream<LocalBiology> seaTileBiologies =
            stream(Optional.ofNullable(nauticalMap))
                .flatMap(map -> map.getAllSeaTilesExcludingLandAsList().stream())
                .map(SeaTile::getBiology);

        final Stream<LocalBiology> fadBiologies =
            stream(Optional.ofNullable(fadMap))
                .flatMap(FadMap::allFads)
                .filter(localBiologyClass::isInstance)
                .map(localBiologyClass::cast);

        final Collection<B> localBiologies =
            Streams.concat(seaTileBiologies, fadBiologies)
                .filter(localBiologyClass::isInstance)
                .map(localBiologyClass::cast)
                .collect(toImmutableList());
        return localBiologies;
    }

    abstract Map<Species, A> aggregate(
        final GlobalBiology globalBiology,
        final Collection<B> localBiologies
    );

}
