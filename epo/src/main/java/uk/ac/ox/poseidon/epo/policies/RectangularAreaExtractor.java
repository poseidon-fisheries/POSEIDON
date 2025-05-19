/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.epo.policies;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.monitors.loggers.RowProvider;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.ConditionalRegulations;
import uk.ac.ox.poseidon.regulations.api.Regulations;
import uk.ac.ox.poseidon.regulations.core.conditions.InRectangularArea;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RectangularAreaExtractor implements RowProvider {
    private final Iterable<? extends List<?>> rows;

    RectangularAreaExtractor(final FishState fishState) {
        this.rows =
            extract(fishState.getRegulations(), Regulations::getSubRegulations)
                .filter(ConditionalRegulations.class::isInstance)
                .map(r -> ((ConditionalRegulations) r).getCondition())
                .flatMap(c -> extract(c, Condition::getSubConditions))
                .filter(InRectangularArea.class::isInstance)
                .map(InRectangularArea.class::cast)
                .map(rect -> ImmutableList.of(
                    rect.getEnvelope().getMinX(),
                    rect.getEnvelope().getMaxX(),
                    rect.getEnvelope().getMinY(),
                    rect.getEnvelope().getMaxY()
                ))
                .collect(Collectors.toList());
    }

    private static <T> Stream<T> extract(
        final T obj,
        final Function<? super T, ? extends Collection<T>> f
    ) {
        return Stream.concat(
            Stream.of(obj),
            f.apply(obj)
                .stream()
                .flatMap(sub -> extract(sub, f))
        );
    }

    @Override
    public List<String> getHeaders() {
        return ImmutableList.of("min_lon", "max_lon", "min_lat", "max_lat");
    }

    @Override
    public Iterable<? extends List<?>> getRows() {
        return rows;
    }

}
