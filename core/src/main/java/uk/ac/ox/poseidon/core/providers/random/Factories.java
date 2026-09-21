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

package uk.ac.ox.poseidon.core.providers.random;

/**
 * Factories for {@link uk.ac.ox.poseidon.core.providers.Provider}s that draw a fresh random value,
 * from the simulation's shared RNG, on every call. Every factory here is per-simulation scoped —
 * see the individual factory classes.
 */
public class Factories {

    private Factories() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @return a factory for a {@link RandomBooleanProvider} with an even (0.5) probability of
     * returning {@code true}
     * @see RandomBooleanProvider
     */
    public static RandomBooleanProviderFactory randomBoolean() {
        return new RandomBooleanProviderFactory(0.5);
    }

    /**
     * @param probability the probability, in {@code [0, 1]}, that the resulting provider returns
     *                    {@code true}
     * @return a factory for a {@link RandomBooleanProvider} with the given probability
     * @see RandomBooleanProvider
     */
    public static RandomBooleanProviderFactory randomBoolean(final double probability) {
        return new RandomBooleanProviderFactory(probability);
    }

    /**
     * @param minimum inclusive lower bound of the returned values
     * @param maximum exclusive upper bound of the returned values
     * @return a factory for a {@link RandomDoubleProvider} uniformly distributed over the range
     * @see RandomDoubleProvider
     */
    public static RandomDoubleProviderFactory randomDouble(
        final double minimum,
        final double maximum
    ) {
        return new RandomDoubleProviderFactory(minimum, maximum);
    }

    /**
     * @param minimum inclusive lower bound of the returned values
     * @param maximum inclusive upper bound of the returned values
     * @return a factory for a {@link RandomIntProvider} uniformly distributed over the range
     * @see RandomIntProvider
     */
    public static RandomIntProviderFactory randomInt(
        final int minimum,
        final int maximum
    ) {
        return new RandomIntProviderFactory(minimum, maximum);
    }

    /**
     * @param mean the mean of the Poisson distribution the resulting provider draws from
     * @return a factory for a {@link RandomPoissonProvider} with the given mean
     * @see RandomPoissonProvider
     */
    public static RandomPoissonProviderFactory randomPoisson(
        final double mean
    ) {
        return new RandomPoissonProviderFactory(mean);
    }
}
