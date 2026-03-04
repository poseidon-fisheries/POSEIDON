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

package uk.ac.ox.poseidon.agents.components;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.agents.vessels.VesselScopeFactory;
import uk.ac.ox.poseidon.core.Factory;

/**
 * Creates per-vessel components and registers them in a shared component register.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ComponentFactory<C> extends VesselScopeFactory<C> {

    private Factory<? super VesselScope, ? extends C> componentFactory;
    private Factory<? super VesselScope, ? extends ComponentRegister<C>> componentRegister;

    @Override
    protected C newInstance(final VesselScope scope) {
        final C component = componentFactory.get(scope);
        final ComponentRegister<C> componentRegister = this.componentRegister.get(scope);
        componentRegister.putComponent(scope.getVessel(), component);
        return component;
    }
}
