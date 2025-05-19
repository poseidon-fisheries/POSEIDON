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

package uk.ac.ox.poseidon.regulations.core.quantities;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.regulations.api.Quantity;

import java.util.Collection;

import static com.google.common.collect.ImmutableList.toImmutableList;

public class SumOfFactory implements ComponentFactory<Quantity> {

    private Collection<ComponentFactory<Quantity>> quantities;

    @SuppressWarnings("unused")
    public SumOfFactory() {
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public SumOfFactory(final ComponentFactory<Quantity>... quantities) {
        this(ImmutableList.copyOf(quantities));
    }

    @SuppressWarnings("WeakerAccess")
    public SumOfFactory(final Collection<ComponentFactory<Quantity>> quantities) {
        this.quantities = quantities;
    }

    public Collection<ComponentFactory<Quantity>> getQuantities() {
        return quantities;
    }

    public void setQuantities(final Collection<ComponentFactory<Quantity>> quantities) {
        this.quantities = quantities;
    }

    @Override
    public Quantity apply(final ModelState modelState) {
        return new SumOf(
            quantities.stream().map(q -> q.apply(modelState)).collect(toImmutableList())
        );
    }
}
