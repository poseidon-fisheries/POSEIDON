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

import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;
import uk.ac.ox.poseidon.core.ScenarioNode;

/**
 * What the columns of a vessel register table mean: four role columns, then
 * the columns that become vessel attributes. A role column is an attribute
 * only if it is also listed among the attribute columns; a listed name column
 * is an ordinary attribute. A column of the table that is neither a role column
 * nor listed is ignored. Built via {@link Factories#vesselRegisterColumns}.
 */
@Value
@Builder(toBuilder = true)
public class VesselRegisterColumns implements ScenarioNode {

  /** Column giving each row's vessel id. */
  @NonNull
  String vesselIdColumn;

  /** Column giving each row's event code. */
  @NonNull
  String eventCodeColumn;

  /** Column giving each row's event date. */
  @NonNull
  String eventDateColumn;

  /** Column giving each row's home-port code, stated as the home port. */
  @NonNull
  String homePortColumn;

  /** The columns whose values become vessel attributes; possibly none. */
  @Singular
  ImmutableList<String> attributeColumns;

  /** @return every column the table has to hold: the roles and the attributes */
  ImmutableList<String> namedColumns() {
    return ImmutableList.<String>builder()
      .add(vesselIdColumn, eventCodeColumn, eventDateColumn, homePortColumn)
      .addAll(attributeColumns)
      .build();
  }
}
