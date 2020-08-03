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

package uk.ac.ox.oxfish.fisher.equipment.gear.fads;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import ec.util.MersenneTwisterFast;

import javax.measure.Quantity;
import javax.measure.quantity.Time;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.HOUR;

public class DurationSampler {

    private static final LoadingCache<List<Double>, DurationSampler> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(args -> {
            checkArgument(checkNotNull(args).size() == 3);
            return new DurationSampler(args.get(0), args.get(1), args.get(2));
        }));

    private final double minimumDurationInHours;
    private final double meanDurationInHours;
    private final double standardDeviationInHours;

    private DurationSampler(
        final double minimumDurationInHours,
        final double meanDurationInHours,
        final double standardDeviationInHours
    ) {
        this.minimumDurationInHours = minimumDurationInHours;
        this.meanDurationInHours = meanDurationInHours;
        this.standardDeviationInHours = standardDeviationInHours;
    }

    public static DurationSampler getInstance(
        final double minimumDurationInHours,
        final double meanDurationInHours,
        final double standardDeviationInHours
    ) {
        return cache.getUnchecked(ImmutableList.of(
            minimumDurationInHours,
            meanDurationInHours,
            standardDeviationInHours
        ));
    }

    public Quantity<Time> nextDuration(MersenneTwisterFast rng) {
        final double duration = Math.max(
            minimumDurationInHours,
            rng.nextGaussian() * standardDeviationInHours + meanDurationInHours
        );
        return getQuantity(duration, HOUR);
    }

}
