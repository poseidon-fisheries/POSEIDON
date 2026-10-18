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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.poseidon.agents.vessels.GivenComponents.componentGiving;
import static uk.ac.ox.poseidon.agents.vessels.GivenComponents.giveComponent;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;

class BestOptionsFromFriendsTest {

  private final Vessel vessel = mock(Vessel.class);

  private static Vessel activeVessel() {
    final Vessel vessel = mock(Vessel.class);
    when(vessel.isActive()).thenReturn(true);
    return vessel;
  }

  private static ExponentialMovingAverageOptionValues<String> valuesOf(
    final Map<String, Double> observations
  ) {
    final ExponentialMovingAverageOptionValues<String> values =
      new ExponentialMovingAverageOptionValues<>(1.0);
    observations.forEach(values::observe);
    return values;
  }

  private static BestOptionsFromFriends<String> bestOptionsFrom(
    final VesselComponent<? extends OptionValues<String>> optionValues,
    final Vessel... friends
  ) {
    return new BestOptionsFromFriends<>(optionValues, friendsOf ->
      List.of(friends)
    );
  }

  @Test
  void aVesselWithNoFriendHasNoOptionFromThem() {
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      activeVessel(),
      valuesOf(Map.of("A", 10.0))
    );

    assertThat(
      bestOptionsFrom(optionValues).apply(vessel).getBestEntries()
    ).isEmpty();
  }

  @Test
  void givesTheBestOptionsOfAFriendsOwnTable() {
    final Vessel friend = activeVessel();
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      friend,
      valuesOf(Map.of("A", 10.0, "B", 20.0))
    );

    assertThat(
      bestOptionsFrom(optionValues, friend).apply(vessel).getBestOptions()
    ).containsExactly("B");
  }

  @Test
  void keepsTheHighestValueOfAnOptionSeveralFriendsKnow() {
    final Vessel friendA = activeVessel();
    final Vessel friendB = activeVessel();
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      friendA,
      valuesOf(Map.of("X", 10.0))
    );
    giveComponent(friendB, optionValues, valuesOf(Map.of("X", 30.0)));

    final OptionValues<String> result = bestOptionsFrom(
      optionValues,
      friendA,
      friendB
    ).apply(vessel);

    assertThat(result.getBestOptions()).containsExactly("X");
    assertThat(result.getBestValue()).hasValue(30.0);
  }

  @Test
  void mergesTheDifferentOptionsOfSeveralFriends() {
    final Vessel friendA = activeVessel();
    final Vessel friendB = activeVessel();
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      friendA,
      valuesOf(Map.of("A", 10.0))
    );
    giveComponent(friendB, optionValues, valuesOf(Map.of("B", 20.0)));

    final OptionValues<String> result = bestOptionsFrom(
      optionValues,
      friendA,
      friendB
    ).apply(vessel);

    assertThat(result.getValue("A")).hasValue(10.0);
    assertThat(result.getValue("B")).hasValue(20.0);
    assertThat(result.getBestOptions()).containsExactly("B");
  }

  @Test
  void skipsAnInactiveFriend() {
    final Vessel friend = mock(Vessel.class);
    when(friend.isActive()).thenReturn(false);
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      friend,
      valuesOf(Map.of("A", 10.0))
    );

    assertThat(
      bestOptionsFrom(optionValues, friend).apply(vessel).getBestEntries()
    ).isEmpty();
  }

  @Test
  void skipsAFriendWithNoTable() {
    final Vessel friendWithTable = activeVessel();
    final Vessel friendWithout = activeVessel();
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      friendWithTable,
      valuesOf(Map.of("A", 10.0))
    );

    assertThat(
      bestOptionsFrom(optionValues, friendWithout, friendWithTable)
        .apply(vessel)
        .getBestOptions()
    ).containsExactly("A");
  }

  @Test
  void aFriendWhoseTableHasNoEntryGivesNoOption() {
    final Vessel friend = activeVessel();
    final VesselComponent<OptionValues<String>> optionValues = componentGiving(
      friend,
      valuesOf(Map.of())
    );

    assertThat(
      bestOptionsFrom(optionValues, friend).apply(vessel).getBestEntries()
    ).isEmpty();
  }

  @Test
  void readsTheFriendsOfTheVesselGivenAndValuesEachFriendsOptionsForThatFriend() {
    final Vessel otherVessel = mock(Vessel.class);
    final Vessel friend = activeVessel();
    final OptionValues<String> friendsOwnValues = new ImmutableOptionValues<>(
      Map.of("A", 10.0)
    );
    final OptionValues<String> valuesForAnotherVessel =
      new ImmutableOptionValues<>(Map.of("B", 20.0));
    final VesselComponent<Function<Vessel, OptionValues<String>>> optionValues =
      componentGiving(friend, valuedFor ->
        valuedFor == friend ? friendsOwnValues : valuesForAnotherVessel
      );
    final BestOptionsFromFriends<String> bestOptionsFromFriends =
      new BestOptionsFromFriends<>(optionValues, friendsOf ->
        friendsOf == vessel ? List.of(friend) : List.of()
      );

    assertThat(
      bestOptionsFromFriends.apply(vessel).getBestOptions()
    ).containsExactly("A");
    assertThat(
      bestOptionsFromFriends.apply(otherVessel).getBestEntries()
    ).isEmpty();
  }
}
