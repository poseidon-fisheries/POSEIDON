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

package uk.ac.ox.oxfish.fisher.log.timeScalarFunctions;

/**
 * The sigmoidal time scalar function.
 * The a parameter controls how soon the function will drop. High values of a means a longer perfect memory window.
 * The b parameter controls how steeply the function will dip when it does. The lower the b is, the less steep.
 * It looks like this:
 * _______
 * \
 * \
 * |
 * |
 * \
 * \_______________ or something like that!
 *
 * @author Brian Powers 5/3/2019
 */
public class SigmoidalTimeScalar implements TimeScalarFunction {
    double a, b;

    public SigmoidalTimeScalar(double a, double b) {
        this.a = a;
        this.b = b;
    }

    public double timeScalar(double t) {
        return (1 + Math.exp(-a)) / (1 + Math.exp(-a + b * t));

    }
}
