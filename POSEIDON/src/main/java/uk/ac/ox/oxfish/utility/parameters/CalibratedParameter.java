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

package uk.ac.ox.oxfish.utility.parameters;

import static com.google.common.base.Preconditions.checkArgument;

public class CalibratedParameter extends UniformDoubleParameter {

    private double defaultValue;
    private double hardMinimum;
    private double hardMaximum;

    public CalibratedParameter() {
        this(0, Integer.MAX_VALUE);
    }

    public CalibratedParameter(
        final double minimumValue,
        final double maximumValue
    ) {
        this(
            minimumValue,
            maximumValue,
            (minimumValue + maximumValue) / 2
        );
    }

    public CalibratedParameter(
        final double minimumValue,
        final double maximumValue,
        final double defaultValue
    ) {
        this(
            minimumValue,
            maximumValue,
            Math.min(minimumValue, 0),
            Math.max(maximumValue, Integer.MAX_VALUE),
            defaultValue
        );
    }

    public CalibratedParameter(
        final double minimumValue,
        final double maximumValue,
        final double hardMinimum,
        final double hardMaximum,
        final double defaultValue
    ) {
        super(minimumValue, maximumValue);
        checkArgument(hardMinimum <= minimumValue);
        checkArgument(hardMaximum >= maximumValue);
        checkArgument(hardMinimum < hardMaximum);
        checkArgument(defaultValue >= hardMinimum);
        checkArgument(defaultValue <= hardMaximum);
        this.hardMinimum = hardMinimum;
        this.hardMaximum = hardMaximum;
        this.defaultValue = defaultValue;
    }

    public CalibratedParameter(final double defaultValue) {
        this(0, Integer.MAX_VALUE, defaultValue);
    }

    public CalibratedParameter(
        final double minimumValue,
        final double maximumValue,
        final double hardMinimum,
        final double hardMaximum
    ) {
        this(
            minimumValue,
            maximumValue,
            hardMinimum,
            hardMaximum,
            (minimumValue + maximumValue) / 2
        );
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

    public double getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(final double defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public CalibratedParameter makeCopy() {
        return new CalibratedParameter(
            getMinimum(),
            getMaximum(),
            hardMinimum,
            hardMaximum,
            defaultValue
        );
    }

}
