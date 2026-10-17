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

package uk.ac.ox.poseidon.agents.vessels;

import java.lang.ref.WeakReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.ac.ox.poseidon.core.ProductStore;
import uk.ac.ox.poseidon.core.Scope;
import uk.ac.ox.poseidon.core.SimulationScope;
import uk.ac.ox.poseidon.core.events.Listener;

/**
 * A {@link SimulationScope} narrowed to a {@link Vessel}, for factories
 * building vessel-owned components. A build registers for its vessel through
 * this scope, which gives the registration's teardown to the scope's store
 * (the vessel's, or a vessel component's in a narrower scope), so that it is
 * taken back when that store ends. Holds the vessel via a
 * {@link WeakReference}, so that scoping to a vessel never itself keeps that
 * vessel alive past its natural lifetime. In tests, keep the vessel in a
 * field, for the reason given in {@link SimulationScope}.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class VesselScope extends SimulationScope {

  private final WeakReference<Vessel> vessel;

  /**
   * Copy constructor: a scope of exactly this class over the same simulation
   * and vessel as {@code vesselScope}, which may be narrower.
   */
  VesselScope(final VesselScope vesselScope) {
    super(vesselScope);
    this.vessel = vesselScope.vessel;
  }

  /**
   * @param simulationScope the simulation scope to narrow
   * @param vessel          the vessel to scope to
   */
  public VesselScope(
    final SimulationScope simulationScope,
    final Vessel vessel
  ) {
    super(simulationScope);
    this.vessel = new WeakReference<>(vessel);
  }

  /**
   * Read only by the behaviour's factory, which gives the vessel to the tree
   * it builds: every other build that needs the vessel takes it on each call,
   * or registers through this scope.
   *
   * @return the scoped vessel, or {@code null} if it has since been
   *         garbage-collected
   */
  public Vessel getVessel() {
    return vessel.get();
  }

  /** @return the scoped vessel's store */
  @Override
  protected ProductStore getProductStore() {
    return getVessel().getProductStore();
  }

  /**
   * Registers a listener on the vessel's event manager, so that it receives
   * the events the vessel broadcasts from now on, until this scope's store
   * ends.
   *
   * @param listener the listener to register
   */
  @Override
  public void addListener(final Listener<?> listener) {
    final Vessel vessel = getVessel();
    vessel.getEventManager().addListener(listener);
    getProductStore().addTeardown(() ->
      vessel.getEventManager().removeListener(listener)
    );
  }

  /**
   * Registers {@code item} for the vessel in {@code registry}, until this
   * scope's store ends.
   *
   * @param registry the structure to register {@code item} in
   * @param item     what to register for the vessel
   * @param <T>      the type of what is registered
   */
  public <T> void register(
    final VesselRegistry<? super T> registry,
    final T item
  ) {
    getProductStore().addTeardown(registry.register(getVessel(), item));
  }

  @Override
  protected Scope at(final Class<? extends Scope> level) {
    return level == VesselScope.class ? new VesselScope(this) : super.at(level);
  }
}
