/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.poseidon.regulations.core;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Regulations;

import java.util.Map;
import java.util.function.Supplier;

import static com.google.common.collect.ImmutableSet.toImmutableSet;

public class NamedRegulationsFactory implements ComponentFactory<Regulations> {
    private Map<String, ComponentFactory<Regulations>> regulations;

    public NamedRegulationsFactory(final Map<String, ComponentFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }

    @SuppressWarnings("unused")
    public NamedRegulationsFactory() {
    }

    @Override
    public Regulations apply(final ModelState modelState) {
        return new ConjunctiveRegulations(
            regulations.values().stream()
                .map(regulation -> regulation.apply(modelState))
                .collect(toImmutableSet())
        );
    }

    public void modify(
        final String regulationName,
        final Supplier<? extends ComponentFactory<Regulations>> supplier
    ) {
        setRegulations(
            ImmutableMap.<String, ComponentFactory<Regulations>>builder()
                .putAll(getRegulations())
                .put(regulationName, supplier.get())
                .buildKeepingLast()
        );
    }

    public Map<String, ComponentFactory<Regulations>> getRegulations() {
        return regulations;
    }

    public void setRegulations(final Map<String, ComponentFactory<Regulations>> regulations) {
        this.regulations = regulations;
    }
}
