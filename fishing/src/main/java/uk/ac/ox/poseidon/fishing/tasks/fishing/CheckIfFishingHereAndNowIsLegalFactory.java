/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025-2026, University of Oxford.
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

package uk.ac.ox.poseidon.fishing.tasks.fishing;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.fishing.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.fishing.tasks.TaskFactory;
import uk.ac.ox.poseidon.fishing.vessels.Vessel;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponent;
import uk.ac.ox.poseidon.fishing.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.fishing.vessels.gears.Gear;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * A {@link TaskFactory} counterpart of
 * {@link CheckIfFishingHereAndNowIsLegal}, built via
 * {@link Factories#checkIfFishingHereAndNowIsLegal}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class CheckIfFishingHereAndNowIsLegalFactory
  extends TaskFactory<CheckIfFishingHereAndNowIsLegal>
{

  private final VesselComponent<? extends Gear> gear;
  private final Factory<
    ? super VesselComponentScope,
    ? extends Regulations<? super ExtendedFishingAction>
  > regulations;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Override
  protected CheckIfFishingHereAndNowIsLegal newInstance(
    final VesselComponentScope scope
  ) {
    return new CheckIfFishingHereAndNowIsLegal(
      gear,
      regulations.get(scope),
      currentDateTime.get(scope)
    );
  }
}
