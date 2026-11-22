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

package uk.ac.ox.poseidon.fishing.vessels;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.collect.ImmutableList;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import uk.ac.ox.poseidon.core.ScenarioNode;

/**
 * The event codes of a vessel register: those of the rows that create a
 * vessel, those that change it and those that make it exit. There is at least
 * one creation code, and no code is in two lists; the change and exit lists
 * may be empty. Built via {@link Factories#vesselRegisterEventCodes}.
 */
@Value
public final class VesselRegisterEventCodes implements ScenarioNode {

  /** Codes whose rows create a vessel. */
  ImmutableList<String> creationCodes;

  /** Codes whose rows change a vessel's attributes or home port. */
  ImmutableList<String> changeCodes;

  /** Codes whose rows make a vessel exit. */
  ImmutableList<String> exitCodes;

  /**
   * @throws IllegalArgumentException if there is no creation code, or a code
   *                                  is in two lists
   */
  @Builder(toBuilder = true)
  private VesselRegisterEventCodes(
    @Singular final ImmutableList<String> creationCodes,
    @Singular final ImmutableList<String> changeCodes,
    @Singular final ImmutableList<String> exitCodes
  ) {
    checkArgument(
      !creationCodes.isEmpty(),
      "A vessel register needs at least one creation code"
    );
    final Set<String> seen = new HashSet<>();
    for (final ImmutableList<String> codes : ImmutableList.of(
      creationCodes,
      changeCodes,
      exitCodes
    )) {
      for (final String code : new HashSet<>(codes)) {
        checkArgument(
          seen.add(code),
          "Event code %s is in more than one list",
          code
        );
      }
    }
    this.creationCodes = creationCodes;
    this.changeCodes = changeCodes;
    this.exitCodes = exitCodes;
  }
}
