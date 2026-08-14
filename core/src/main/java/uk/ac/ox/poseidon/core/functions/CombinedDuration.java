/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.core.functions;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

public class CombinedDuration<T> implements Function<T, Duration> {

    private final Function<T, ? extends Duration>[] durations;

    @SuppressWarnings("unchecked")
    public CombinedDuration(final List<? extends Function<? super T, ? extends Duration>> durations) {
        this.durations = durations.toArray(new Function[0]);
    }

    @Override
    public Duration apply(final T t) {
        Duration total = Duration.ZERO;
        for (final Function<T, ? extends Duration> duration : durations) {
            total = total.plus(duration.apply(t));
        }
        return total;
    }
}
