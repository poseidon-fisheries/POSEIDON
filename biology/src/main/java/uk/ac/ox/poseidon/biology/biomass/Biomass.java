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

import lombok.Data;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.biology.Content;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkNonNegative;

@Data
public class Biomass implements Content<Biomass> {

    // Biomass stored internally in kilograms
    private final double biomassInKg;

    public Biomass(final double biomassInKg) {
        this.biomassInKg = checkNonNegative(biomassInKg, "Biomass");
    }

    public Biomass(
        final Number value,
        final Unit<Mass> unit
    ) {
        this(
            Quantities
                .getQuantity(value, unit)
                .to(KILOGRAM)
                .getValue()
                .doubleValue()
        );
    }

    public static Biomass ofKg(final double value) {
        return new Biomass(value);
    }

    public double as(final Unit<Mass> biomassUnit) {
        return this.asQuantity().to(biomassUnit).getValue().doubleValue();
    }

    public Biomass add(final Biomass content) {
        return new Biomass(this.biomassInKg + content.biomassInKg);
    }

    public Biomass subtract(final Biomass content) {
        return new Biomass(this.biomassInKg - content.biomassInKg);
    }

    public Biomass multiply(final double value) {
        return new Biomass(this.biomassInKg * value);
    }

    public Biomass divide(final double value) {
        return new Biomass(this.biomassInKg / value);
    }

    public boolean isEmpty() {
        return this.biomassInKg == 0;
    }

    @Override
    public Biomass asBiomass() {
        return this;
    }

    public Quantity<Mass> asQuantity() {
        return Quantities.getQuantity(this.biomassInKg, KILOGRAM);
    }

    public String toString() {return "Biomass(" + this.asQuantity() + ")";}

    public double asKg() {
        return this.biomassInKg;
    }
}
