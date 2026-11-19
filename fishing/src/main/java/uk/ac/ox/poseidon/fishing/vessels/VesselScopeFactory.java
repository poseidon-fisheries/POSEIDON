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

package uk.ac.ox.poseidon.fishing.vessels;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import uk.ac.ox.poseidon.core.AbstractFactory;
import uk.ac.ox.poseidon.core.Factory;

/**
 * A {@link Factory} whose produced object is cached per {@link Vessel} — one
 * instance per vessel agent, shared by every resolution against that vessel for
 * the life of the simulation, but never shared between different vessels or
 * across simulation runs.
 * <p>
 * Declare this level only when the product needs one instance per vessel: it
 * holds mutable state of its own, its build registers through the vessel's
 * scope, or its build has side effects on what its inputs give it. A stateless
 * product that only reads a vessel's state is a {@link
 * uk.ac.ox.poseidon.core.RelativeScopeFactory}, per vessel when an input is.
 */
@Getter
@ToString
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class VesselScopeFactory<P> extends AbstractFactory<
  VesselScope,
  P
> {

  @Override
  protected Class<VesselScope> scopeClass() {
    return VesselScope.class;
  }
}
