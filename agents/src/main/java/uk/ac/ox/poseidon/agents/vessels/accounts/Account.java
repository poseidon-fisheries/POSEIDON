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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.ToString;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

@Getter
@ToString
public class Account {
    @SuppressFBWarnings(
        value = "EI_EXPOSE_REP2",
        justification = "CurrencyUnit is immutable, safe to expose"
    )
    private final CurrencyUnit currencyUnit;
    private Money balance;

    public Account(final CurrencyUnit currencyUnit) {
        this.currencyUnit = checkNotNull(currencyUnit);
        this.balance = Money.zero(currencyUnit);
    }

    public void setBalance(final Money balance) {
        this.balance = checkCurrency(balance);
    }

    public void add(final Money amount) {
        this.balance = balance.plus(checkCurrency(amount));
    }

    public void subtract(final Money amount) {
        checkArgument(amount.getCurrencyUnit().equals(currencyUnit));
        this.balance = balance.minus(checkCurrency(amount));
    }

    private Money checkCurrency(final Money amount) {
        checkNotNull(amount);
        checkArgument(
            amount.getCurrencyUnit().equals(currencyUnit),
            "Expected currency %s, got %s.",
            currencyUnit.getCode(),
            amount.getCurrencyUnit().getCode()
        );
        return amount;
    }

    public BigDecimal getBalanceAmount() {
        return balance.getAmount();
    }
}
