/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.AllArgsConstructor;
import lombok.Getter;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.biology.Content;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static javax.measure.MetricPrefix.KILO;
import static tech.units.indriya.unit.Units.GRAM;

@AllArgsConstructor
@Getter
public class Biomass implements Content<Biomass> {

    private Quantity<Mass> quantity;

    public Biomass(
        final Number value,
        final Unit<Mass> unit
    ) {
        this(Quantities.getQuantity(value, unit));
    }

    public static Biomass ofKg(final double value) {
        return new Biomass(value, KILO(GRAM));
    }

    public Biomass add(final Biomass content) {
        return new Biomass(quantity.add(content.quantity));
    }

    public Biomass subtract(final Biomass content) {
        return new Biomass(quantity.subtract(content.quantity));
    }

    public boolean isEmpty() {
        return quantity.isEquivalentTo(Quantities.getQuantity(0, KILO(GRAM)));
    }

    @Override
    public Biomass asBiomass() {
        return this;
    }

    public double asKg() {
        return quantity.to(KILO(GRAM)).getValue().doubleValue();
    }
}
