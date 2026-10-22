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

package uk.ac.ox.poseidon.fishing.vessels.values;

import java.time.LocalDateTime;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselScope;
import uk.ac.ox.poseidon.fishing.vessels.VesselScopeFactory;

/**
 * A {@link VesselScopeFactory} counterpart of
 * {@link CurrentPortStayStartDateTime}, which it registers on the vessel's
 * event manager. Each vessel gets one, kept for the vessel's life. The current
 * date-time when it is built is taken as the start of the vessel's first stay
 * at port, so it must first be built for a vessel at the vessel's creation, as
 * it is when a component the vessel specification lists uses it. A component
 * chosen only after a change of attributes is built later, and would date that
 * first stay from the change. Built via
 * {@link Factories#currentPortStayStartDateTime()}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CurrentPortStayStartDateTimeFactory
  extends VesselScopeFactory<Function<Vessel, LocalDateTime>>
  implements ValueFactory<VesselScope, Vessel, LocalDateTime>
{

  private final ValueFactory<
    ? super VesselScope,
    ? super Void,
    LocalDateTime
  > currentDateTime;

  @Override
  protected Function<Vessel, LocalDateTime> newInstance(
    final VesselScope scope
  ) {
    final CurrentPortStayStartDateTime currentPortStayStartDateTime =
      new CurrentPortStayStartDateTime(currentDateTime.get(scope).apply(null));
    scope.addListener(currentPortStayStartDateTime);
    return currentPortStayStartDateTime;
  }
}
