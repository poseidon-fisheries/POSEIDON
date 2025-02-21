/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.core.aggregators;

import java.util.Arrays;
import java.util.Collection;
import java.util.OptionalDouble;
import java.util.function.Function;
import java.util.stream.DoubleStream;
import java.util.stream.Stream;

public interface Aggregator extends Function<DoubleStream, OptionalDouble> {

    default OptionalDouble apply(final Stream<? extends Number> numbers) {
        return apply(numbers.mapToDouble(Number::doubleValue));
    }

    default OptionalDouble apply(final Collection<? extends Number> numbers) {
        return apply(numbers.stream());
    }

    default OptionalDouble apply(final double[] numbers) {
        return apply(Arrays.stream(numbers));
    }

}
