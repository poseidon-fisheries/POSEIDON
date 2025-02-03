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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import tech.units.indriya.format.SimpleQuantityFormat;
import tech.units.indriya.format.SimpleUnitFormat;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;

import javax.measure.Quantity;
import javax.measure.Unit;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public abstract class AbstractQuantityFactory<Q extends Quantity<Q>>
    extends GlobalScopeFactory<Quantity<Q>> {

    private final Class<Q> type;
    private double value;
    private String unitString;

    public AbstractQuantityFactory(
        final Class<Q> type,
        final String quantity
    ) {
        this.type = type;
        final Quantity<Q> q = SimpleQuantityFormat.getInstance().parse(quantity).asType(type);
        this.value = q.getValue().doubleValue();
        this.unitString = q.getUnit().toString();
    }

    @Override
    protected Quantity<Q> newInstance(final Simulation simulation) {
        final Unit<Q> unit = SimpleUnitFormat.getInstance().parse(unitString).asType(type);
        return Quantities.getQuantity(value, unit);
    }
}
