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

package uk.ac.ox.oxfish.utility.adaptation;

import uk.ac.ox.oxfish.model.FishState;

/**
 * memorizes last sensor output and returns it without updating for
 * a specified number of years
 */
public class IntermittentSensorDecorator<T> implements Sensor<FishState, T> {

    private static final long serialVersionUID = 4215633694359433508L;
    private final Sensor<FishState, T> delegate;
    private final int minInterval;
    private T lastScan;
    private int lastYearCalled;

    public IntermittentSensorDecorator(
        final Sensor<FishState, T> delegate,
        final int minInterval
    ) {
        this.delegate = delegate;
        this.minInterval = minInterval;
    }

    @Override
    public T scan(final FishState system) {

        if (lastScan == null ||
            system.getYear() - lastYearCalled >= minInterval) {
            lastScan = delegate.scan(system);
            lastYearCalled = system.getYear();
        }
        return lastScan;


    }
}
