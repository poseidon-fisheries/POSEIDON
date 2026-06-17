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
