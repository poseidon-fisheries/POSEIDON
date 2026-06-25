/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.agents.vessels.friends;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselsGetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.data.Offset.offset;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DynamicFriendsSupplierTest {

    private static final int TARGET_FRIENDS = 3;
    private MersenneTwisterFast rng;

    @BeforeEach
    void setUp() {
        rng = new MersenneTwisterFast(42);
    }

    private static Vessel mockVessel(final boolean active) {
        final Vessel vessel = mock(Vessel.class);
        when(vessel.isActive()).thenReturn(active);
        return vessel;
    }

    private static Vessel mockVessel() {
        return mockVessel(true);
    }

    @Test
    void testEmptyPoolReturnsEmpty() {
        final Vessel self = mockVessel();
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            TARGET_FRIENDS, List::of, v -> true, self, rng
        );
        assertThat(supplier.get()).isEmpty();
    }

    @Test
    void testReturnsUpToTargetWhenPoolSufficient() {
        final Vessel self = mockVessel();
        final List<Vessel> pool = List.of(mockVessel(), mockVessel(), mockVessel(), mockVessel(), mockVessel());
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            TARGET_FRIENDS, () -> pool, v -> true, self, rng
        );
        assertThat(supplier.get()).hasSize(TARGET_FRIENDS);
    }

    @Test
    void testReturnsAllWhenPoolSmallerThanTarget() {
        final Vessel self = mockVessel();
        final List<Vessel> pool = List.of(mockVessel(), mockVessel());
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            TARGET_FRIENDS, () -> pool, v -> true, self, rng
        );
        assertThat(supplier.get()).hasSize(2);
    }

    @Test
    void testExcludesSelf() {
        final Vessel self = mockVessel();
        final List<Vessel> pool = List.of(mockVessel(), mockVessel(), mockVessel());
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            TARGET_FRIENDS, () -> pool, v -> true, self, rng
        );
        assertThat(supplier.get()).doesNotContain(self);
    }

    @Test
    void testFiltersByCondition() {
        final Vessel self = mockVessel();
        final Vessel active = mockVessel(true);
        final Vessel inactive = mockVessel(false);
        final List<Vessel> pool = List.of(active, inactive);
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            1, () -> pool, v -> ((Vessel) v).isActive(), self, rng
        );
        assertThat(supplier.get()).containsExactly(active);
    }

    @Test
    void testFriendsPersistAcrossCalls() {
        final Vessel self = mockVessel();
        final List<Vessel> pool = List.of(mockVessel(), mockVessel(), mockVessel(), mockVessel(), mockVessel());
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            TARGET_FRIENDS, () -> pool, v -> true, self, rng
        );
        final Set<Vessel> first = supplier.get();
        final Set<Vessel> second = supplier.get();
        assertThat(first).isEqualTo(second);
    }

    @Test
    void testPrunesFriendsThatNoLongerSatisfyCondition() {
        final Vessel self = mockVessel();
        final Vessel friend = mockVessel(true);
        final Predicate<Vessel> isActive = v -> ((Vessel) v).isActive();
        final List<Vessel> pool = new ArrayList<>(List.of(friend));
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            1, () -> pool, isActive, self, rng
        );

        assertThat(supplier.get()).containsExactly(friend);

        // Friend becomes inactive
        when(friend.isActive()).thenReturn(false);

        assertThat(supplier.get()).doesNotContain(friend);
    }

    @Test
    void testReplenishesAfterPruning() {
        final Vessel self = mockVessel();
        final Vessel friendA = mockVessel(true);
        final Vessel friendB = mockVessel(true);
        final Vessel friendC = mockVessel(true);
        final Predicate<Vessel> isActive = v -> ((Vessel) v).isActive();
        final List<Vessel> pool = new ArrayList<>(List.of(friendA, friendB, friendC));
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            2, () -> pool, isActive, self, rng
        );

        // Initial fill: gets 2 friends
        final Set<Vessel> first = supplier.get();
        assertThat(first).hasSize(2);

        // One friend becomes inactive; pool still has the other
        final Vessel removed = first.iterator().next();
        when(removed.isActive()).thenReturn(false);
        pool.remove(removed);

        // Replenishment: should drop the inactive one and still have 2
        final Set<Vessel> second = supplier.get();
        assertThat(second).hasSize(2).doesNotContain(removed);
    }

    @Test
    void testReturnsUnmodifiableSet() {
        final Vessel self = mockVessel();
        final List<Vessel> pool = List.of(mockVessel(), mockVessel(), mockVessel());
        final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
            TARGET_FRIENDS, () -> pool, v -> true, self, rng
        );
        assertThatThrownBy(() -> supplier.get().add(mockVessel()))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testSamplingIsApproximatelyUniform() {
        final int poolSize = 20;
        final int sampleSize = 5;
        final int trials = 200_000;
        final double tolerance = 0.02;
        final double expectedRate = (double) sampleSize / poolSize;

        final Vessel self = mockVessel();
        final List<Vessel> pool = new ArrayList<>();
        for (int i = 0; i < poolSize; i++) {
            pool.add(mockVessel());
        }
        final MersenneTwisterFast uniformRng = new MersenneTwisterFast(123);

        final int[] counts = new int[poolSize];
        for (int t = 0; t < trials; t++) {
            // Fresh supplier each trial so sampling happens from scratch
            final DynamicFriendsSupplier supplier = new DynamicFriendsSupplier(
                sampleSize, () -> pool, v -> true, self, uniformRng
            );
            for (final Vessel v : supplier.get()) {
                final int idx = pool.indexOf(v);
                if (idx >= 0) counts[idx]++;
            }
        }

        for (int i = 0; i < poolSize; i++) {
            final double rate = (double) counts[i] / trials;
            assertThat(rate)
                .as("Item " + i + " appears at rate " + rate + ", expected " + expectedRate)
                .isCloseTo(expectedRate, offset(tolerance));
        }
    }

}
