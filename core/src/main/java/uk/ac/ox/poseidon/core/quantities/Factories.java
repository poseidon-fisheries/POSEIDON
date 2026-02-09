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

import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.quantities.AbstractQuantityFactory.parse;

public class Factories {

    private Factories() {
    }

    public static <S extends Scope> KilogramsFactory<S> kilograms(
        final Factory<? super S, ? extends Quantity<Mass>> mass
    ) {
        return new KilogramsFactory<>(mass);
    }

    public static KilogramsFactory<Scope> kilograms(final double value) {
        return new KilogramsFactory<>(massOf(value, KILOGRAM));
    }

    public static MassFactory massOf(
        final double value,
        final Unit<Mass> unit
    ) {
        return new MassFactory(value, unit.toString());
    }

    public static MassFactory massOf(final Quantity<Mass> quantity) {
        return massOf(quantity.getValue().doubleValue(), quantity.getUnit());
    }

    public static MassFactory massOf(final String quantity) {
        final var entry = parse(Mass.class, quantity);
        return new MassFactory(entry.getValue(), entry.getKey());
    }

}
