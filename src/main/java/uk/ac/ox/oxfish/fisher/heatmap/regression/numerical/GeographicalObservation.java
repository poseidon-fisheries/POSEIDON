/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.heatmap.regression.numerical;

import com.google.common.base.MoreObjects;
import uk.ac.ox.oxfish.geography.SeaTile;

import java.util.Objects;

/**
 * An immutable object useful for regression. It is sorted with respect to time
 * Created by carrknight on 6/27/16.
 */
public class GeographicalObservation<V> implements Comparable<GeographicalObservation<V>>
{

    private final double time;

    private final V value;

    private final SeaTile tile;

    public GeographicalObservation(SeaTile tile, double time, V value) {
        this.tile=tile;
        this.time = time;
        this.value = value;
    }


    /**
     *  the tile x coordinate
     */
    public double getXCoordinate() {
        return tile.getGridX();
    }

    /**
     *  the tile y coordinate
     */
    public double getYCoordinate() {
        return tile.getGridY();
    }

    /**
     * Getter for property 'time'.
     *
     * @return Value for property 'time'.
     */
    public double getTime() {
        return time;
    }

    /**
     * Getter for property 'value'.
     *
     * @return Value for property 'value'.
     */
    public V getValue() {
        return value;
    }

    /**
     * Note: this class has a natural ordering that is
     * inconsistent with equals: the ordering is over time alone
     */
    @Override
    public int compareTo(GeographicalObservation o) {
        return Double.compare(this.getTime(),o.getTime());
    }

    /**
     * Getter for property 'tile'.
     *
     * @return Value for property 'tile'.
     */
    public SeaTile getTile() {
        return tile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeographicalObservation<V> that = (GeographicalObservation) o;
        return Double.compare(that.time, time) == 0 &&
                Objects.equals(that.value, value)  &&
                Objects.equals(tile, that.tile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, value, tile);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("time", time)
                .add("value", value)
                .add("tile", tile)
                .toString();
    }
}
