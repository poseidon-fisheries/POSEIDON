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

package uk.ac.ox.poseidon.agents.tasks.fishing;

import static uk.ac.ox.poseidon.core.providers.temporal.Factories.currentDateTime;

import uk.ac.ox.poseidon.agents.catches.disposition.DispositionProcess;
import uk.ac.ox.poseidon.agents.regulations.actions.ExtendedFishingAction;
import uk.ac.ox.poseidon.agents.vessels.Vessel;
import uk.ac.ox.poseidon.agents.vessels.VesselComponent;
import uk.ac.ox.poseidon.agents.vessels.VesselComponentScope;
import uk.ac.ox.poseidon.agents.vessels.gears.Gear;
import uk.ac.ox.poseidon.agents.vessels.holds.Hold;
import uk.ac.ox.poseidon.agents.vessels.tanks.FuelTank;
import uk.ac.ox.poseidon.biology.Fisheable;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.ValueFactory;
import uk.ac.ox.poseidon.regulations.Regulations;

/**
 * Factories for fishing behavior-tree tasks and their supporting event
 * accumulator.
 */
public class Factories {

  private Factories() {}

  /**
   * @param gear        the declaration of the gear the vessel would fish with
   * @param regulations the regulations a fishing action started now, here, is
   *                    checked against
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for a
   *         {@link CheckIfFishingHereAndNowIsLegal}
   * @see CheckIfFishingHereAndNowIsLegal
   */
  public static CheckIfFishingHereAndNowIsLegalFactory checkIfFishingHereAndNowIsLegal(
    final VesselComponent<? extends Gear> gear,
    final Factory<
      ? super VesselComponentScope,
      ? extends Regulations<? super ExtendedFishingAction>
    > regulations
  ) {
    return new CheckIfFishingHereAndNowIsLegalFactory(
      gear,
      regulations,
      currentDateTime()
    );
  }

  /**
   * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a
   *         {@link FishingEventAccumulator}
   * @see FishingEventAccumulator
   */
  public static FishingEventAccumulatorFactory fishingEventAccumulator() {
    return new FishingEventAccumulatorFactory();
  }

  /**
   * @param propertyPath the dotted path of properties to follow from the
   *                     fishing event, e.g. {@code "action.gear"} for the gear
   *                     the fishing action was made with
   * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for an
   *         {@link uk.ac.ox.poseidon.core.functions.ObjectProperty} of a
   *         fishing event
   * @see FishingEventPropertyFactory
   */
  public static <R> FishingEventPropertyFactory<R> fishingEventProperty(
    final String propertyPath
  ) {
    return new FishingEventPropertyFactory<>(propertyPath);
  }

  /**
   * @param gear               the declaration of the gear to fish with
   * @param hold               the declaration of the hold the catch goes in
   * @param fuelTank           the declaration of the tank the fuel fishing
   *                           burns is taken from
   * @param fisheable          the fisheable to fish from, read for the vessel
   * @param dispositionProcess sorts the catch into retained/discarded
   * @return a {@link uk.ac.ox.poseidon.agents.tasks.TaskFactory} for a {@link Fishing} task
   * @see Fishing
   */
  public static FishingFactory fishing(
    final VesselComponent<? extends Gear> gear,
    final VesselComponent<? extends Hold> hold,
    final VesselComponent<? extends FuelTank> fuelTank,
    final ValueFactory<
      ? super VesselComponentScope,
      ? super Vessel,
      Fisheable
    > fisheable,
    final Factory<
      ? super VesselComponentScope,
      ? extends DispositionProcess
    > dispositionProcess
  ) {
    return new FishingFactory(
      gear,
      hold,
      fuelTank,
      fisheable,
      dispositionProcess,
      currentDateTime()
    );
  }
}
