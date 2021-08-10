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

package uk.ac.ox.oxfish.biology.tuna;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.stream;

import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;

/**
 * Objects of this class can take a collection of local biologies and "aggregate" then (i.e., sum
 * them) into a single local biology object.
 *
 * <p>In most cases, the collection of local biologies will be all the {@link SeaTile} biologies
 * plus all the {@link uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad} biologies.</p>
 *
 * @param <B> The type of local biology to aggregate.
 */
public abstract class Aggregator<B extends LocalBiology> {

    private final Class<B> localBiologyClass;

    Aggregator(final Class<B> localBiologyClass) {
        this.localBiologyClass = localBiologyClass;
    }

    public Class<B> getLocalBiologyClass() {
        return localBiologyClass;
    }

    public B aggregate(
        final GlobalBiology globalBiology,
        @Nullable final NauticalMap nauticalMap,
        @Nullable final FadMap fadMap
    ) {
        return aggregate(globalBiology, getLocalBiologies(nauticalMap, fadMap));
    }

    abstract B aggregate(
        final GlobalBiology globalBiology,
        final Collection<B> localBiologies
    );

    @SuppressWarnings("UnstableApiUsage")
    private Collection<B> getLocalBiologies(
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

        return Streams.concat(seaTileBiologies, fadBiologies)
            .filter(localBiologyClass::isInstance)
            .map(localBiologyClass::cast)
            .collect(toImmutableList());
    }

}
