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

package uk.ac.ox.poseidon.agents.vessels.accounts;

import lombok.Getter;
import lombok.ToString;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.util.HashMap;
import java.util.Map;

@Getter
@ToString
public class Account {

    private final Map<CurrencyUnit, Money> balances = new HashMap<>();

    public void setBalance(final Money balance) {
        balances.put(balance.getCurrencyUnit(), balance);
    }

    public void add(final Money amount) {
        balances.put(
            amount.getCurrencyUnit(),
            balances
                .getOrDefault(amount.getCurrencyUnit(), Money.zero(amount.getCurrencyUnit()))
                .plus(amount)
        );
    }

    public void subtract(final Money amount) {
        balances.put(
            amount.getCurrencyUnit(),
            balances
                .getOrDefault(amount.getCurrencyUnit(), Money.zero(amount.getCurrencyUnit()))
                .minus(amount)
        );
    }

    public void add(final Account other) {
        other.balances.values().forEach(this::add);
    }

}
