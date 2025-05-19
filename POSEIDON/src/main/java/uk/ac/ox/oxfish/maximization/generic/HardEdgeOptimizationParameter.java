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
package uk.ac.ox.oxfish.maximization.generic;

import com.google.common.base.Preconditions;

/**
 * this is just like simple optimization parameter, but it has additional minimum and maximum which are never crossed
 * even if the EVA optimization allows for bounds to go beyond their usual -10,10 co-domain
 */
public class HardEdgeOptimizationParameter extends SimpleOptimizationParameter {

    private static final long serialVersionUID = 2757943884587032368L;
    private double hardMinimum = Integer.MIN_VALUE;
    private double hardMaximum = Integer.MAX_VALUE;

    public HardEdgeOptimizationParameter(
        final String addressToModify,
        final double minimum,
        final double maximum,
        final boolean isRawNumber,
        final double hardMinimum,
        final double hardMaximum
    ) {
        super(addressToModify, minimum, maximum, hardMinimum >= 0, isRawNumber);
        this.hardMinimum = hardMinimum;
        this.hardMaximum = hardMaximum;
    }

    public HardEdgeOptimizationParameter() {
    }

    @Override
    public double computeNumericValue(final double input) {

        Preconditions.checkArgument(
            hardMinimum < hardMaximum,
            super.getAddressToModify() + "has hard edges that are inconsistent"
        );

        final double original = super.computeNumericValue(input);
        if (original < hardMinimum)
            return hardMinimum;
        if (original > hardMaximum)
            return hardMaximum;
        return original;
    }

    public double getHardMinimum() {
        return hardMinimum;
    }

    public void setHardMinimum(final double hardMinimum) {
        this.hardMinimum = hardMinimum;
    }

    public double getHardMaximum() {
        return hardMaximum;
    }

    public void setHardMaximum(final double hardMaximum) {
        this.hardMaximum = hardMaximum;
    }

    @Override
    public String toString() {
        return "HardEdgeOptimizationParameter{" +
            "addressToModify='" + getAddressToModify() + '\'' +
            ", minimum=" + getMinimum() +
            ", maximum=" + getMaximum() +
            ", alwaysPositive=" + isAlwaysPositive() +
            ", isRawNumber=" + isRawNumber() +
            ", hardMinimum=" + hardMinimum +
            ", hardMaximum=" + hardMaximum +
            '}';
    }
}
