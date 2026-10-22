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
 * {@link HasStartedFishingTripToday}, which it registers on the vessel's event
 * manager. Each vessel gets one, kept for the vessel's life, so the date of its
 * last fishing trip outlives a rebuild of the component that reads it, such as
 * its behaviour after a change of gear. Built via
 * {@link Factories#hasStartedFishingTripToday()}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class HasStartedFishingTripTodayFactory
  extends VesselScopeFactory<Function<Vessel, Boolean>>
  implements ValueFactory<VesselScope, Vessel, Boolean>
{

  private final ValueFactory<
    ? super VesselScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Override
  protected Function<Vessel, Boolean> newInstance(final VesselScope scope) {
    final HasStartedFishingTripToday hasStartedFishingTripToday =
      new HasStartedFishingTripToday(currentDateTime.get(scope));
    scope.addListener(hasStartedFishingTripToday);
    return hasStartedFishingTripToday;
  }
}
