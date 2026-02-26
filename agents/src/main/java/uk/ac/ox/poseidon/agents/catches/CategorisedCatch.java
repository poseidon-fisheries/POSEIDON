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

package uk.ac.ox.poseidon.agents.catches;

import com.google.common.collect.ImmutableMap;
import lombok.Value;
import uk.ac.ox.poseidon.biology.buckets.Bucket;

import java.util.HashMap;
import java.util.Map;

@Value
public class CategorisedCatch {

    public static CategorisedCatch empty() {
        return new CategorisedCatch(ImmutableMap.of());
    }

    public CategorisedCatch(final Map<CatchCategory, Bucket> buckets) {
        this.buckets = ImmutableMap.copyOf(buckets);
    }

    ImmutableMap<CatchCategory, Bucket> buckets;

    public CategorisedCatch add(final CategorisedCatch other) {
        if (buckets.isEmpty()) return other;
        if (other.buckets.isEmpty()) return this;

        final Map<CatchCategory, Bucket> merged =
            new HashMap<>(buckets.size() + other.buckets.size());
        merged.putAll(buckets);
        for (final var entry : other.buckets.entrySet()) {
            merged.merge(entry.getKey(), entry.getValue(), Bucket::add);
        }
        return new CategorisedCatch(merged);
    }

    public boolean isEmpty() {
        if (buckets.isEmpty()) return true;
        for (final Bucket bucket : buckets.values()) {
            if (!bucket.isEmpty()) return false;
        }
        return true;
    }

    public double getTotalBiomassInKg() {
        double totalBiomassInKg = 0.0;
        for (final Bucket bucket : buckets.values()) {
            totalBiomassInKg += bucket.getTotalBiomassInKg();
        }
        return totalBiomassInKg;
    }

}
