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

package uk.ac.ox.poseidon.regulations.core.conditions;

import uk.ac.ox.poseidon.common.api.ComponentFactory;
import uk.ac.ox.poseidon.common.api.ModelState;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.regulations.api.Condition;
import uk.ac.ox.poseidon.regulations.api.Quantity;

public class BelowFactory implements ComponentFactory<Condition> {

    private ComponentFactory<? extends Quantity> quantity;
    private DoubleParameter limit;

    public BelowFactory() {
    }

    public BelowFactory(
        final ComponentFactory<? extends Quantity> quantity,
        final double limit
    ) {
        this(quantity, new FixedDoubleParameter(limit));
    }

    @SuppressWarnings("WeakerAccess")
    public BelowFactory(
        final ComponentFactory<? extends Quantity> quantity,
        final DoubleParameter limit
    ) {
        this.limit = limit;
        this.quantity = quantity;
    }

    public ComponentFactory<? extends Quantity> getQuantity() {
        return quantity;
    }

    public void setQuantity(final ComponentFactory<? extends Quantity> quantity) {
        this.quantity = quantity;
    }

    public DoubleParameter getLimit() {
        return limit;
    }

    public void setLimit(final DoubleParameter limit) {
        this.limit = limit;
    }

    @Override
    public Condition apply(final ModelState modelState) {
        return new uk.ac.ox.poseidon.regulations.core.conditions.Below(
            limit.applyAsDouble(modelState.getRandom()),
            quantity.apply(modelState)
        );
    }
}
