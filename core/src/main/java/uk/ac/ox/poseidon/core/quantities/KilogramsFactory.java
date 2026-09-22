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

package uk.ac.ox.poseidon.core.quantities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;

import static tech.units.indriya.unit.Units.KILOGRAM;

/**
 * A {@link RelativeScopeFactory} for the numeric value, in kilograms, of a resolved {@link Mass}
 * quantity — a convenience unwrapper for code that wants a plain {@code double} rather than a
 * JSR-385 {@link Quantity}. No separate plain component class here: the produced value is a bare
 * {@link Double}, with no wrapper to carry documentation. Built via
 * {@link Factories Factories.kilograms(...)}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KilogramsFactory<S extends Scope> extends RelativeScopeFactory<S, Double> {

    private Factory<? super S, ? extends Quantity<Mass>> mass;

    @Override
    protected Double newInstance(final S scope) {
        return mass.get(scope)
            .to(KILOGRAM)
            .getValue()
            .doubleValue();
    }

}
