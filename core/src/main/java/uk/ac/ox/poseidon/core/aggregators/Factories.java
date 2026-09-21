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

package uk.ac.ox.poseidon.core.aggregators;

/**
 * Factories for {@link Aggregator}s that reduce a stream, collection, or array of numbers to a
 * single summary value.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link MeanAggregator}
     * @see MeanAggregator
     */
    public static MeanAggregatorFactory meanAggregator() {
        return new MeanAggregatorFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link MinAggregator}
     * @see MinAggregator
     */
    public static MinAggregatorFactory minAggregator() {
        return new MinAggregatorFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link MaxAggregator}
     * @see MaxAggregator
     */
    public static MaxAggregatorFactory maxAggregator() {
        return new MaxAggregatorFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link MedianAggregator}
     * @see MedianAggregator
     */
    public static MedianAggregatorFactory medianAggregator() {
        return new MedianAggregatorFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link SumAggregator}
     * @see SumAggregator
     */
    public static SumAggregatorFactory sumAggregator() {
        return new SumAggregatorFactory();
    }

}
