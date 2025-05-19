/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.log;

import uk.ac.ox.oxfish.geography.SeaTile;

public class LocationMemory<T> {
    private final SeaTile spot;

    private final T information;

    private int memoryAge;

    public LocationMemory(SeaTile spot, T information) {
        this.spot = spot;
        this.information = information;
        memoryAge = 0;
    }

    public SeaTile getSpot() {
        return spot;
    }

    public T getInformation() {
        return information;
    }

    public int getMemoryAge() {
        return memoryAge;
    }

    public int age() {
        return ++memoryAge;
    }
}
