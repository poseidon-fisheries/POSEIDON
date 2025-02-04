/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.core.quantities;

import javax.measure.Unit;
import javax.measure.quantity.Mass;

public class MassFactory extends AbstractQuantityFactory<Mass> {

    public MassFactory() {
        super(Mass.class);
    }

    public MassFactory(
        final double value,
        final String unitString
    ) {
        super(Mass.class, value, unitString);
    }

    public MassFactory(
        final double value,
        final Unit<Mass> unit
    ) {
        super(Mass.class, value, unit.toString());
    }

    public static MassFactory of(final String quantity) {
        final var entry = parse(Mass.class, quantity);
        return new MassFactory(entry.getValue(), entry.getKey());
    }

}
