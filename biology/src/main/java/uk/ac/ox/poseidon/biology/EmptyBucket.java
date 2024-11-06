/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.biology;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.biology.species.Species;

import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class EmptyBucket<C extends Content<C>> implements Bucket<C> {
    @Override
    public C getContent(final Species species) {
        return null;
    }

    @Override
    public Optional<C> maybeGetContent(final Species species) {
        return Optional.empty();
    }

    @Override
    public Bucket<C> replaceContent(
        final Species species,
        final C newContent
    ) {
        return new ImmutableBucket<>(ImmutableMap.of(species, newContent));
    }

    @Override
    public Map<Species, C> getMap() {
        return Map.of();
    }

    @Override
    public Bucket<C> mapContent(final UnaryOperator<C> mapper) {
        return this;
    }
}
