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

import com.google.common.collect.Streams;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Basically a way to extract some or all of the local biologies (i.e., both sea tile biologies and
 * FAD biologies from the {@link FishState}. It does the nice job of filtering on the given type of
 * local biology, which makes it typesafe and automatically filter out, say the {@link
 * uk.ac.ox.oxfish.biology.EmptyLocalBiology} objects.
 *
 * @param <B> The type of local biology to extract.
 */
public class LocalBiologiesExtractor<B extends LocalBiology>
    implements Function<FishState, List<B>> {

    private final Class<B> localBiologyClass;
    private final boolean includeFads;
    private final boolean includeSeaTiles;
    private final CacheByFishState<List<B>> cache =
        new CacheByFishState<>(this::extractLocalBiologies);

    /**
     * Creates a {@link LocalBiologiesExtractor}.
     *
     * @param localBiologyClass The class object for the type of biology we want to extract.
     * @param includeFads       Whether or not to include FAD biologies.
     * @param includeSeaTiles   Whether or not to include sea tile biologies.
     */
    public LocalBiologiesExtractor(
        final Class<B> localBiologyClass,
        final boolean includeFads,
        final boolean includeSeaTiles
    ) {
        this.localBiologyClass = localBiologyClass;
        this.includeFads = includeFads;
        this.includeSeaTiles = includeSeaTiles;
    }

    public Class<B> getLocalBiologyClass() {
        return localBiologyClass;
    }

    @Override
    public List<B> apply(final FishState fishState) {
        return cache.get(fishState);
    }

    private List<B> extractLocalBiologies(final FishState fishState) {

        final Stream<LocalBiology> seaTileBiologies =
            (includeSeaTiles ? Stream.of(fishState.getMap()) : Stream.<NauticalMap>empty())
                .flatMap(map -> map.getAllSeaTilesExcludingLandAsList().stream())
                .map(SeaTile::getBiology);

        final Stream<FadMap<?, ?>> fadMapStream = includeFads
            ? Stream.of(fishState.getFadMap())
            : Stream.empty();
        final Stream<LocalBiology> fadBiologies =
            fadMapStream
                .flatMap(FadMap::allFads)
                .filter(localBiologyClass::isInstance)
                .map(localBiologyClass::cast);

        return Streams.concat(seaTileBiologies, fadBiologies)
            .filter(localBiologyClass::isInstance)
            .map(localBiologyClass::cast)
            .collect(toImmutableList());
    }

}
