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

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.fads.FadMap;
import uk.ac.ox.oxfish.model.FishState;

abstract class FadBiologyExcluder<B extends LocalBiology> extends Excluder<B> {

    FadBiologyExcluder(final Aggregator<B> aggregator) {
        super(aggregator);
    }

    @Override
    Collection<B> getBiologiesToExclude(final FishState fishState) {
        //noinspection UnstableApiUsage
        return stream(Optional.ofNullable(fishState.getFadMap()))
            .flatMap(this::getFads)
            .map(Fad::getBiology)
            .collect(toImmutableList());
    }

    abstract Stream<? extends Fad<B>> getFads(FadMap fadMap);

}
