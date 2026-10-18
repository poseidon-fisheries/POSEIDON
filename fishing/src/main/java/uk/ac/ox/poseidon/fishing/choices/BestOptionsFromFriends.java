/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.choices;

import static lombok.AccessLevel.PACKAGE;

import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;

/**
 * Gives the best options across a vessel's active friends' own
 * {@link OptionValues} components, each valued for that friend, skipping a
 * friend that has no such component, and merging ties
 * by keeping the highest value seen for each option. Reuses a single
 * {@link ReusableOptionValues} instance across calls — the returned
 * {@link OptionValues} is only valid until the next call to {@link #apply}.
 */
@RequiredArgsConstructor(access = PACKAGE)
class BestOptionsFromFriends<O> implements Function<Vessel, OptionValues<O>> {

  private final VesselComponent<
    ? extends Function<? super Vessel, ? extends OptionValues<O>>
  > optionValues;
  private final Function<
    ? super Vessel,
    ? extends Iterable<? extends Vessel>
  > friends;
  private final ReusableOptionValues<O> aggregatedValues =
    new ReusableOptionValues<>();

  /**
   * @param vessel the vessel whose friends' best options are asked for
   * @return every active friend's best options, merged by keeping the highest
   *         value for each option; the same reused instance every call,
   *         invalidated by the next call
   */
  @Override
  public OptionValues<O> apply(final Vessel vessel) {
    aggregatedValues.clear();
    for (final Vessel friend : friends.apply(vessel)) {
      if (!friend.isActive()) continue;
      optionValues
        .findIn(friend)
        .ifPresent(values ->
          values.apply(friend).forEachBestEntry(aggregatedValues::putIfGreater)
        );
    }
    return aggregatedValues;
  }
}
