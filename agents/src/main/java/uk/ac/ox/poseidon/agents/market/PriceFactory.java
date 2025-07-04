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

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import uk.ac.ox.poseidon.core.GlobalScopeFactory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.utils.Measurements;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.math.RoundingMode.HALF_EVEN;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceFactory extends GlobalScopeFactory<Price> {

    private double amount;
    private String currencyUnit;
    private String massUnit;

    @Override
    protected Price newInstance(final Simulation simulation) {
        checkNotNull(currencyUnit, "currencyUnit must not be null");
        checkNotNull(massUnit, "massUnit must not be null");
        return new Price(
            Money.of(
                CurrencyUnit.of(currencyUnit),
                amount,
                HALF_EVEN
            ),
            Measurements.parseMassUnit(massUnit)
        );
    }
}
