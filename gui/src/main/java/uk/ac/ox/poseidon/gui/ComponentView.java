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

package uk.ac.ox.poseidon.gui;

import lombok.Value;
import sim.util.Proxiable;
import uk.ac.ox.poseidon.core.Simulation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.toImmutableList;

/**
 * One row of the model inspector's component list: a named simulation component, shown by a
 * short summary instead of its own {@link Object#toString()}, which can be arbitrarily large (a
 * component may hold hundreds of thousands of elements) and which MASON would otherwise rebuild
 * at every inspector refresh. Inspecting the row inspects the component itself, through
 * {@link Proxiable}.
 */
@Value
public class ComponentView implements Proxiable {

    /** The name the component was registered under. */
    String name;
    /** The resolved component. */
    Object component;

    /**
     * @param simulation the simulation whose components to list
     * @return one view per component of {@code simulation}, in registration order
     */
    public static List<ComponentView> of(final Simulation simulation) {
        return simulation
            .getComponents()
            .entrySet()
            .stream()
            .map(entry -> new ComponentView(entry.getKey(), entry.getValue()))
            .collect(toImmutableList());
    }

    /** @return the component, which MASON inspects in place of this view */
    @Override
    public Object propertiesProxy() {
        return component;
    }

    /**
     * @return the component's name and type, plus its size if it is a collection or a map; never
     * the component's own string representation
     */
    @Override
    public String toString() {
        final String type = component == null ? "null" : component.getClass().getSimpleName();
        final String size = switch (component) {
            case final Collection<?> collection -> " (" + collection.size() + ")";
            case final Map<?, ?> map -> " (" + map.size() + ")";
            case null, default -> "";
        };
        return name + ": " + type + size;
    }
}
