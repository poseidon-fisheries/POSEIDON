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

package uk.ac.ox.poseidon.agents.choices;

import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegister;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

import java.util.List;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BestOptionsFromFriendsTest {

    @Test
    void testGetWhenEmptyRegister() {
        final VesselComponentRegister<OptionValues<String>> register =
            new VesselComponentRegister<>();
        final Supplier<List<Vessel>> friendsSupplier = List::of;
        final BestOptionsFromFriends<String> supplier =
            new BestOptionsFromFriends<>(register, friendsSupplier);
        assertThat(supplier.get().getBestEntries()).isEmpty();
    }

    @Test
    void testGetWithSingleFriend() {
        final Port port = mock(Port.class);
        final Vessel friend = mock(Vessel.class);
        when(friend.isActive()).thenReturn(true);
        when(friend.getHomePort()).thenReturn(port);

        final AverageOptionValues<String> friendValues = new AverageOptionValues<>();
        friendValues.observe("A", 10.0);
        friendValues.observe("B", 20.0);

        final VesselComponentRegister<OptionValues<String>> register =
            new VesselComponentRegister<>();
        register.putComponent(friend, friendValues);

        final Supplier<List<Vessel>> friendsSupplier = () -> List.of(friend);
        final BestOptionsFromFriends<String> supplier =
            new BestOptionsFromFriends<>(register, friendsSupplier);

        assertThat(supplier.get().getBestOptions()).containsExactly("B");
    }

    @Test
    void testGetWithMultipleFriendsMergeSameOption() {
        final Port port = mock(Port.class);
        final Vessel friendA = mock(Vessel.class);
        when(friendA.isActive()).thenReturn(true);
        when(friendA.getHomePort()).thenReturn(port);

        final Vessel friendB = mock(Vessel.class);
        when(friendB.isActive()).thenReturn(true);
        when(friendB.getHomePort()).thenReturn(port);

        final AverageOptionValues<String> valuesA = new AverageOptionValues<>();
        valuesA.observe("X", 10.0);

        final AverageOptionValues<String> valuesB = new AverageOptionValues<>();
        valuesB.observe("X", 30.0);

        final VesselComponentRegister<OptionValues<String>> register =
            new VesselComponentRegister<>();
        register.putComponent(friendA, valuesA);
        register.putComponent(friendB, valuesB);

        final Supplier<List<Vessel>> friendsSupplier = () -> List.of(friendA, friendB);
        final BestOptionsFromFriends<String> supplier =
            new BestOptionsFromFriends<>(register, friendsSupplier);

        assertThat(supplier.get().getBestOptions()).containsExactly("X");
        assertThat(supplier.get().getBestValue()).hasValue(30.0);
    }

    @Test
    void testGetWithInactiveFriend() {
        final Port port = mock(Port.class);
        final Vessel friend = mock(Vessel.class);
        when(friend.isActive()).thenReturn(false);
        when(friend.getHomePort()).thenReturn(port);

        final AverageOptionValues<String> friendValues = new AverageOptionValues<>();
        friendValues.observe("A", 10.0);

        final VesselComponentRegister<OptionValues<String>> register =
            new VesselComponentRegister<>();
        register.putComponent(friend, friendValues);

        final Supplier<List<Vessel>> friendsSupplier = () -> List.of(friend);
        final BestOptionsFromFriends<String> supplier =
            new BestOptionsFromFriends<>(register, friendsSupplier);

        assertThat(supplier.get().getBestEntries()).isEmpty();
    }

    @Test
    void testGetWithFriendHavingNoBestEntries() {
        final Port port = mock(Port.class);
        final Vessel friend = mock(Vessel.class);
        when(friend.isActive()).thenReturn(true);
        when(friend.getHomePort()).thenReturn(port);

        final VesselComponentRegister<OptionValues<String>> register =
            new VesselComponentRegister<>();
        register.putComponent(friend, new AverageOptionValues<>());

        final Supplier<List<Vessel>> friendsSupplier = () -> List.of(friend);
        final BestOptionsFromFriends<String> supplier =
            new BestOptionsFromFriends<>(register, friendsSupplier);

        assertThat(supplier.get().getBestEntries()).isEmpty();
    }

    @Test
    void testGetWithMultipleFriendsWithDifferentOptions() {
        final Port port = mock(Port.class);
        final Vessel friendA = mock(Vessel.class);
        when(friendA.isActive()).thenReturn(true);
        when(friendA.getHomePort()).thenReturn(port);

        final Vessel friendB = mock(Vessel.class);
        when(friendB.isActive()).thenReturn(true);
        when(friendB.getHomePort()).thenReturn(port);

        final AverageOptionValues<String> valuesA = new AverageOptionValues<>();
        valuesA.observe("A", 10.0);

        final AverageOptionValues<String> valuesB = new AverageOptionValues<>();
        valuesB.observe("B", 20.0);

        final VesselComponentRegister<OptionValues<String>> register =
            new VesselComponentRegister<>();
        register.putComponent(friendA, valuesA);
        register.putComponent(friendB, valuesB);

        final Supplier<List<Vessel>> friendsSupplier = () -> List.of(friendA, friendB);
        final BestOptionsFromFriends<String> supplier =
            new BestOptionsFromFriends<>(register, friendsSupplier);

        final OptionValues<String> result = supplier.get();
        assertThat(result.getValue("A")).hasValue(10.0);
        assertThat(result.getValue("B")).hasValue(20.0);
        assertThat(result.getBestOptions()).containsExactly("B");
    }
}
