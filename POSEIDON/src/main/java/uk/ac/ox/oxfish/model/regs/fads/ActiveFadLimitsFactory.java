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
import com.google.common.primitives.ImmutableIntArray;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.Streams.zip;
import static tech.units.indriya.unit.Units.CUBIC_METRE;
import static uk.ac.ox.oxfish.model.regs.fads.IATTC.capacityClass;

@SuppressWarnings("UnstableApiUsage")
public class ActiveFadLimitsFactory implements AlgorithmFactory<ActiveFadLimits> {

    // since ActiveFadsLimit has no mutable internal state, we can cache and reuse instances
    private final Map<ImmutableIntArray, ActiveFadLimits> cache = new HashMap<>();

    private final Collection<Predicate<Fisher>> predicates = ImmutableList.of(
        fisher -> capacityClass(fisher) == 1,
        fisher -> capacityClass(fisher) == 2,
        fisher -> capacityClass(fisher) == 3,
        fisher -> capacityClass(fisher) == 4,
        fisher -> capacityClass(fisher) == 5,
        fisher -> capacityClass(fisher) == 6 && fisher.getHold().getVolumeIn(CUBIC_METRE) < 1200,
        fisher -> capacityClass(fisher) == 6 && fisher.getHold().getVolumeIn(CUBIC_METRE) >= 1200
    );

    // These make my heart bleed, but it's the easiest way to make that class YAML-convenient
    // The default values are the current IATTC FadLimits.
    private int limitClass1 = 70;
    private int limitClass2 = 70;
    private int limitClass3 = 70;
    private int limitClass4 = 120;
    private int limitClass5 = 120;
    private int limitClass6a = 300;
    private int limitClass6b = 450;

    public ActiveFadLimitsFactory(
        final int limitClass1to3,
        final int limitClass4to5,
        final int limitClass6a,
        final int limitClass6b
    ) {
        this(
            limitClass1to3,
            limitClass1to3,
            limitClass1to3,
            limitClass4to5,
            limitClass4to5,
            limitClass6a,
            limitClass6b
        );
    }

    public ActiveFadLimitsFactory(
        final int limitClass1,
        final int limitClass2,
        final int limitClass3,
        final int limitClass4,
        final int limitClass5,
        final int limitClass6a,
        final int limitClass6b
    ) {
        this.limitClass1 = limitClass1;
        this.limitClass2 = limitClass2;
        this.limitClass3 = limitClass3;
        this.limitClass4 = limitClass4;
        this.limitClass5 = limitClass5;
        this.limitClass6a = limitClass6a;
        this.limitClass6b = limitClass6b;
    }

    public ActiveFadLimitsFactory() {
    }

    @SuppressWarnings("unused")
    public int getLimitClass1() {
        return limitClass1;
    }

    @SuppressWarnings("unused")
    public void setLimitClass1(final int limitClass1) {
        this.limitClass1 = limitClass1;
    }

    @SuppressWarnings("unused")
    public int getLimitClass2() {
        return limitClass2;
    }

    @SuppressWarnings("unused")
    public void setLimitClass2(final int limitClass2) {
        this.limitClass2 = limitClass2;
    }

    @SuppressWarnings("unused")
    public int getLimitClass3() {
        return limitClass3;
    }

    @SuppressWarnings("unused")
    public void setLimitClass3(final int limitClass3) {
        this.limitClass3 = limitClass3;
    }

    @SuppressWarnings("unused")
    public int getLimitClass4() {
        return limitClass4;
    }

    @SuppressWarnings("unused")
    public void setLimitClass4(final int limitClass4) {
        this.limitClass4 = limitClass4;
    }

    @SuppressWarnings("unused")
    public int getLimitClass5() {
        return limitClass5;
    }

    @SuppressWarnings("unused")
    public void setLimitClass5(final int limitClass5) {
        this.limitClass5 = limitClass5;
    }

    @SuppressWarnings("unused")
    public int getLimitClass6a() {
        return limitClass6a;
    }

    @SuppressWarnings("unused")
    public void setLimitClass6a(final int limitClass6a) {
        this.limitClass6a = limitClass6a;
    }

    @SuppressWarnings("unused")
    public int getLimitClass6b() {
        return limitClass6b;
    }

    @SuppressWarnings("unused")
    public void setLimitClass6b(final int limitClass6b) {
        this.limitClass6b = limitClass6b;
    }

    @Override
    public ActiveFadLimits apply(FishState fishState) {
        final ImmutableIntArray limits = ImmutableIntArray.of(
            limitClass1,
            limitClass2,
            limitClass3,
            limitClass4,
            limitClass5,
            limitClass6a,
            limitClass6b
        );
        return cache.computeIfAbsent(limits, __ -> new ActiveFadLimits(
            zip(predicates.stream(), limits.stream().boxed(), FadLimit::new).collect(toImmutableList())
        ));
    }

}
