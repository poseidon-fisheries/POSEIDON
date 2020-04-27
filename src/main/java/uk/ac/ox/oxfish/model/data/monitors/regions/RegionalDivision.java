/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.model.data.monitors.regions;

import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Collection;

public interface RegionalDivision {

    Collection<Region> getRegions();
    Region getRegion(int gridX, int gridY);
    default Region getRegion(SeaTile seaTile) {
        return getRegion(seaTile.getGridX(), seaTile.getGridY());
    }

    class Region {

        private final int number;
        private final String name;

        Region(int number, String name) {
            this.name = name;
            this.number = number;
        }

        @Override public String toString() { return getName(); }

        public int getNumber() { return number; }

        public String getName() { return name; }

    }

}
