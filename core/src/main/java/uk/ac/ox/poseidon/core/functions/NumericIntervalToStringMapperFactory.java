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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import java.util.List;

/**
 * Factory for {@link NumericIntervalMapper} instances with string outputs.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NumericIntervalToStringMapperFactory
    extends GlobalScopeFactory<NumericIntervalMapper<String>> {

    private List<Interval> intervals;

    public static Interval interval(
        final Double lowerBoundInclusive,
        final Double upperBoundExclusive,
        final String value
    ) {
        return new Interval(lowerBoundInclusive, upperBoundExclusive, value);
    }

    @Override
    protected NumericIntervalMapper<String> newInstance(final Scope scope) {
        return new NumericIntervalMapper<>(
            intervals.stream()
                .map(interval ->
                    new NumericIntervalMapper.Interval<>(
                        interval.getLowerBoundInclusive(),
                        interval.getUpperBoundExclusive(),
                        interval.getValue()
                    )
                )
                .toList()
        );
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Interval {

        // Null means unbounded, matching the runtime mapper interval semantics.
        private Double lowerBoundInclusive;
        private Double upperBoundExclusive;
        private String value;
    }
}
