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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.agents.catches.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.tasks.TaskFactory;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.agents.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;

/**
 * A {@link TaskFactory} counterpart of {@link Fishing}, built via
 * {@link Factories#fishing}.
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@SuperBuilder(toBuilder = true)
public class FishingFactory extends TaskFactory<Fishing> {

  private final VesselComponent<? extends Gear> gear;
  private final VesselComponent<? extends Hold> hold;
  private final VesselComponent<? extends FuelTank> fuelTank;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    Fisheable
  > fisheable;
  private final Factory<
    ? super VesselComponentScope,
    ? extends DispositionProcess
  > dispositionProcess;
  private final ValueFactory<
    ? super VesselComponentScope,
    ? super Vessel,
    LocalDateTime
  > currentDateTime;

  @Override
  protected Fishing newInstance(final VesselComponentScope scope) {
    return new Fishing(
      gear,
      hold,
      fuelTank,
      fisheable.get(scope),
      dispositionProcess.get(scope),
      currentDateTime.get(scope)
    );
  }
}
