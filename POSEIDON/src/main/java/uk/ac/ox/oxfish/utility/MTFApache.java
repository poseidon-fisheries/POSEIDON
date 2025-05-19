/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.utility;

/**
 * Adaptor class for using the MersenneTwisterFast random number generator
 * with the Apache Commons Math distributions.
 * Copied from the MASON user manual, v. 20, section 3.1.2 (p. 61).
 */
public class MTFApache implements org.apache.commons.math3.random.RandomGenerator {
    ec.util.MersenneTwisterFast random;

    public MTFApache(ec.util.MersenneTwisterFast random) {
        this.random = random;
    }

    public boolean nextBoolean() {
        return random.nextBoolean();
    }

    public void nextBytes(byte[] bytes) {
        random.nextBytes(bytes);
    }

    public double nextDouble() {
        return random.nextDouble();
    }

    public float nextFloat() {
        return random.nextFloat();
    }

    public double nextGaussian() {
        return random.nextGaussian();
    }

    public int nextInt() {
        return random.nextInt();
    }

    public int nextInt(int n) {
        return random.nextInt(n);
    }

    public long nextLong() {
        return random.nextLong();
    }

    public void setSeed(int seed) {
        random.setSeed(seed);
    }

    public void setSeed(int[] array) {
        random.setSeed(array);
    }

    public void setSeed(long seed) {
        random.setSeed(seed);
    }
}
