/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.strategies.departing;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.strategies.departing.factory.LogisticDepartingStrategy;
import uk.ac.ox.oxfish.model.FishState;

/**
 * A simple departing strategy that checks how much cash was made in a given period and compares it against a given target.
 * The lower
 * Created by carrknight on 9/11/15.
 */
public class CashFlowLogisticDepartingStrategy extends LogisticDepartingStrategy {


    private static final long serialVersionUID = 6578231870710138020L;
    private final double cashflowTarget;

    private final int cashflowPeriod;


    /**
     * @param l              the L parameter of the logistic curve
     * @param k              the k parameter of the logistic curve
     * @param x0             the x0 parameter of the logistic curve
     * @param cashflowTarget the cashflow  level that satisfies the fisher
     * @param cashflowPeriod
     */
    public CashFlowLogisticDepartingStrategy(
        final double l, final double k, final double x0,
        final double cashflowTarget, final int cashflowPeriod
    ) {
        super(l, k, x0);
        this.cashflowTarget = cashflowTarget;
        this.cashflowPeriod = cashflowPeriod;
    }


    public double getCashflowTarget() {
        return cashflowTarget;
    }

    public int getCashflowPeriod() {
        return cashflowPeriod;
    }


    /**
     * abstract method, returns whatever we need to plug in the logistic function
     *
     * @param fisher the fisher making the decision
     * @param model  the state
     * @param model  a link to the model
     */
    @Override
    public double computeX(final Fisher fisher, final FishState model) {
        final double cash = fisher.getBankBalance();
        final double previousCash = fisher.numberOfDailyObservations() > cashflowPeriod ?
            fisher.balanceXDaysAgo(cashflowPeriod) :
            0;

        double x = (cash - previousCash) / cashflowTarget;
        x = Math.max(x, 0);
        return x;
    }
}
