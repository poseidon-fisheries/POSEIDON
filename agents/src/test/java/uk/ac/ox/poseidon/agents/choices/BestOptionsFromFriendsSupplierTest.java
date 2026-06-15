package uk.ac.ox.poseidon.agents.choices;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegister;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.geography.ports.Port;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BestOptionsFromFriendsSupplierTest {

    @Test
    void testGetWhenEmptyRegister() {
        final Vessel vessel = mock(Vessel.class);
        final VesselComponentRegister<OptionValues<String>> register = new VesselComponentRegister<>();
        final BestOptionsFromFriendsSupplier<String> supplier =
            new BestOptionsFromFriendsSupplier<>(vessel, 10, register, new MersenneTwisterFast(0));
        assertThat(supplier.get().getBestEntries()).isEmpty();
    }

    @Test
    void testGetWithSingleFriend() {
        final Port port = mock(Port.class);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getHomePort()).thenReturn(port);

        final Vessel friend = mock(Vessel.class);
        when(friend.isActive()).thenReturn(true);
        when(friend.getHomePort()).thenReturn(port);

        final AverageOptionValues<String> friendValues = new AverageOptionValues<>();
        friendValues.observe("A", 10.0);
        friendValues.observe("B", 20.0);

        final VesselComponentRegister<OptionValues<String>> register = new VesselComponentRegister<>();
        register.putComponent(friend, friendValues);

        final BestOptionsFromFriendsSupplier<String> supplier =
            new BestOptionsFromFriendsSupplier<>(vessel, 10, register, new MersenneTwisterFast(0));

        assertThat(supplier.get().getBestOptions()).containsExactly("B");
    }

    @Test
    void testGetWithMultipleFriendsMergeSameOption() {
        final Port port = mock(Port.class);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getHomePort()).thenReturn(port);

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

        final VesselComponentRegister<OptionValues<String>> register = new VesselComponentRegister<>();
        register.putComponent(friendA, valuesA);
        register.putComponent(friendB, valuesB);

        final BestOptionsFromFriendsSupplier<String> supplier =
            new BestOptionsFromFriendsSupplier<>(vessel, 10, register, new MersenneTwisterFast(0));

        assertThat(supplier.get().getBestOptions()).containsExactly("X");
        assertThat(supplier.get().getBestValue()).hasValue(30.0);
    }

    @Test
    void testGetWithInactiveFriend() {
        final Port port = mock(Port.class);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getHomePort()).thenReturn(port);

        final Vessel friend = mock(Vessel.class);
        when(friend.isActive()).thenReturn(false);
        when(friend.getHomePort()).thenReturn(port);

        final AverageOptionValues<String> friendValues = new AverageOptionValues<>();
        friendValues.observe("A", 10.0);

        final VesselComponentRegister<OptionValues<String>> register = new VesselComponentRegister<>();
        register.putComponent(friend, friendValues);

        final BestOptionsFromFriendsSupplier<String> supplier =
            new BestOptionsFromFriendsSupplier<>(vessel, 10, register, new MersenneTwisterFast(0));

        assertThat(supplier.get().getBestEntries()).isEmpty();
    }

    @Test
    void testGetWithFriendHavingNoBestEntries() {
        final Port port = mock(Port.class);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getHomePort()).thenReturn(port);

        final Vessel friend = mock(Vessel.class);
        when(friend.isActive()).thenReturn(true);
        when(friend.getHomePort()).thenReturn(port);

        final VesselComponentRegister<OptionValues<String>> register = new VesselComponentRegister<>();
        register.putComponent(friend, new AverageOptionValues<>());

        final BestOptionsFromFriendsSupplier<String> supplier =
            new BestOptionsFromFriendsSupplier<>(vessel, 10, register, new MersenneTwisterFast(0));

        assertThat(supplier.get().getBestEntries()).isEmpty();
    }

    @Test
    void testGetWithMultipleFriendsWithDifferentOptions() {
        final Port port = mock(Port.class);
        final Vessel vessel = mock(Vessel.class);
        when(vessel.getHomePort()).thenReturn(port);

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

        final VesselComponentRegister<OptionValues<String>> register = new VesselComponentRegister<>();
        register.putComponent(friendA, valuesA);
        register.putComponent(friendB, valuesB);

        final BestOptionsFromFriendsSupplier<String> supplier =
            new BestOptionsFromFriendsSupplier<>(vessel, 10, register, new MersenneTwisterFast(0));

        final OptionValues<String> result = supplier.get();
        assertThat(result.getValue("A")).hasValue(10.0);
        assertThat(result.getValue("B")).hasValue(20.0);
        assertThat(result.getBestOptions()).containsExactly("B");
    }
}
