/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024-2025, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import lombok.EqualsAndHashCode;
import tech.units.indriya.quantity.Quantities;
import uk.ac.ox.poseidon.biology.Content;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static tech.units.indriya.unit.Units.KILOGRAM;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkNonNegative;
import static uk.ac.ox.poseidon.core.utils.Preconditions.checkPositive;

/**
 * A single quantity of fish mass, immutable and always non-negative. The canonical, concrete
 * {@link Content} implementation — every other {@code Content} method reduces to one of these.
 */
@EqualsAndHashCode
public class Biomass implements Content {

    public static final Biomass ZERO = new Biomass(0);

    // Biomass stored internally in kilograms
    private final double biomassInKg;

    /**
     * @param biomassInKg the mass, in kilograms; must be non-negative
     */
    public Biomass(final double biomassInKg) {
        this.biomassInKg = checkNonNegative(biomassInKg, "Biomass");
    }

    /**
     * @param value the mass, in {@code unit}; must convert to a non-negative number of kilograms
     * @param unit  the unit {@code value} is expressed in
     */
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

    /**
     * @param value the mass, in kilograms; must be non-negative
     * @return a new {@link Biomass} of {@code value} kilograms
     */
    public static Biomass ofKg(final double value) {
        return new Biomass(value);
    }

    @Override
    public double as(final Unit<Mass> biomassUnit) {
        return this.asQuantity().to(biomassUnit).getValue().doubleValue();
    }

    /**
     * @return the sum of this and {@code content}
     */
    public Biomass add(final Biomass content) {
        return new Biomass(this.biomassInKg + content.biomassInKg);
    }

    /**
     * @param content the amount to subtract; must not exceed this biomass, or the resulting
     *                 negative mass is rejected by the constructor
     * @return this minus {@code content}
     */
    public Biomass subtract(final Biomass content) {
        return new Biomass(this.biomassInKg - content.biomassInKg);
    }

    /**
     * @param value the non-negative factor to scale by
     */
    @Override
    public Biomass multiply(final double value) {
        checkNonNegative(value, "Amount by which to multiply biomass");
        return new Biomass(this.biomassInKg * value);
    }

    /**
     * @param value the strictly positive divisor; {@code 0} is rejected rather than silently
     *              producing an infinite biomass
     */
    @Override
    public Biomass divide(final double value) {
        checkPositive(value, "Amount by which to divide biomass");
        return new Biomass(this.biomassInKg / value);
    }

    public boolean isEmpty() {
        return this.biomassInKg == 0;
    }

    @Override
    public Biomass asBiomass() {
        return this;
    }

    @Override
    public Quantity<Mass> asQuantity() {
        return Quantities.getQuantity(this.biomassInKg, KILOGRAM);
    }

    public String toString() {return "Biomass(" + this.asQuantity() + ")";}

    @Override
    public double asKg() {
        return this.biomassInKg;
    }
}
