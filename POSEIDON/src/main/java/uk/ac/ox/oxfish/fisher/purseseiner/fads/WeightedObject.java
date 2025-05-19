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

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import java.util.Map.Entry;

/**
 * an helper class, really a pair, containing a reference to something with its weight.
 * Better however is to think of it as a weight number with a reference to what is being weighted
 */
public class WeightedObject<KEY> extends Number {

    private static final long serialVersionUID = -5677501710132261821L;
    private final KEY objectBeingWeighted;

    private final double totalWeight;

    public WeightedObject(final KEY objectBeingWeighted, final double totalWeight) {
        this.objectBeingWeighted = objectBeingWeighted;
        this.totalWeight = totalWeight;
    }

    public static <K, N extends Number> WeightedObject<K> from(final Entry<K, N> entry) {
        return new WeightedObject<>(entry.getKey(), entry.getValue().doubleValue());
    }

    public KEY getObjectBeingWeighted() {
        return objectBeingWeighted;
    }

    public double getTotalWeight() {
        return totalWeight;
    }


    @Override
    public int intValue() {
        return (int) totalWeight;
    }

    @Override
    public long longValue() {
        return (long) totalWeight;
    }

    @Override
    public float floatValue() {
        return (float) totalWeight;
    }

    @Override
    public double doubleValue() {
        return totalWeight;
    }
}
