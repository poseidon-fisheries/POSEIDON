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

package uk.ac.ox.poseidon.agents.market;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import org.joda.money.Money;
import uk.ac.ox.poseidon.biology.Content;

import javax.measure.Unit;
import javax.measure.quantity.Mass;

import static java.math.RoundingMode.HALF_EVEN;
import static tech.units.indriya.quantity.Quantities.getQuantity;
import static tech.units.indriya.unit.Units.KILOGRAM;

/** A price per unit of biomass, e.g. "40000 IDR / kg". */
@Value
public class Price {

    /** The amount paid per {@link #biomassUnit}. */
    @NonNull Money amount;
    /** The unit of mass {@link #amount} is quoted per. */
    @NonNull Unit<Mass> biomassUnit;

    @Getter(AccessLevel.NONE)
    double amountPerKg;

    /**
     * @param amount      the amount paid per {@code biomassUnit}
     * @param biomassUnit the unit of mass {@code amount} is quoted per
     */
    @SuppressFBWarnings(
        value = "EI2",
        justification = "Price stores Money/Unit references by design; they are treated as value objects."
    )
    public Price(
        final @NonNull Money amount,
        final @NonNull Unit<Mass> biomassUnit
    ) {
        this.amount = amount;
        this.biomassUnit = biomassUnit;
        final double kgPerUnit =
            getQuantity(1, biomassUnit).to(KILOGRAM).getValue().doubleValue();
        this.amountPerKg = amount.getAmount().doubleValue() / kgPerUnit;
    }

    /** @return this price's value for {@code content}'s biomass */
    public Money valueFor(final Content content) {
        final double value = amountPerKg * content.asKg();
        return Money.of(amount.getCurrencyUnit(), value, HALF_EVEN);
    }

    /** @return this price's value for {@code kg} kilograms, as a raw double (no rounding) */
    public double valueForKgDouble(final double kg) {
        return amountPerKg * kg;
    }

    @java.lang.Override
    public String toString() {
        return amount + " / " + biomassUnit;
    }
}
