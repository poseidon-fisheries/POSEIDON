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

package uk.ac.ox.oxfish.model.regs.fads;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.HashMap;
import java.util.function.Predicate;

import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static uk.ac.ox.oxfish.model.regs.fads.IATTC.capacityClass;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

public class ActiveFadLimitsFactory implements AlgorithmFactory<ActiveFadLimits> {

    public static final ImmutableList<SimpleImmutableEntry<Predicate<Fisher>, Integer>> iattcLimits =
        ImmutableList.of(
            makeLimit(ImmutableSet.of(6), v -> v >= 1200, 450),
            makeLimit(ImmutableSet.of(6), v -> v < 1200, 300),
            makeLimit(ImmutableSet.of(4, 5), __ -> true, 120),
            makeLimit(ImmutableSet.of(1, 2, 3), __ -> true, 70)
        );

    // since ActiveFadsLimit has no mutable internal state, we can cache and reuse instances
    private final HashMap<ImmutableList<SimpleImmutableEntry<Predicate<Fisher>, Integer>>, ActiveFadLimits> cache =
        new HashMap<>();

    private ImmutableList<SimpleImmutableEntry<Predicate<Fisher>, Integer>> limits;

    public ActiveFadLimitsFactory() { this(iattcLimits); }

    public ActiveFadLimitsFactory(ImmutableList<SimpleImmutableEntry<Predicate<Fisher>, Integer>> limits) {
        this.limits = limits;
    }

    public static SimpleImmutableEntry<Predicate<Fisher>, Integer> makeLimit(
        ImmutableSet<Integer> capacityClasses,
        Predicate<Double> volumePredicate,
        int limit
    ) {
        return entry(
            fisher -> capacityClasses.contains(capacityClass(fisher)) &&
                volumePredicate.test(fisher.getHold().getVolumeIn(CUBIC_METRE)),
            limit
        );
    }

    @SuppressWarnings("unused") public ImmutableList<SimpleImmutableEntry<Predicate<Fisher>, Integer>> getLimits() { return limits; }

    @SuppressWarnings("unused") public void setLimits(ImmutableList<SimpleImmutableEntry<Predicate<Fisher>, Integer>> limits) { this.limits = limits; }

    @Override public ActiveFadLimits apply(FishState fishState) {
        return cache.computeIfAbsent(limits, __ -> new ActiveFadLimits(limits));
    }
}
