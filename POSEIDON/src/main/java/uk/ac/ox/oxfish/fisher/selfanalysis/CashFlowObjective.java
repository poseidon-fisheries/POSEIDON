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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * The more money is gained in a fixed number of days, the better!
 * Created by carrknight on 8/4/15.
 */
public class CashFlowObjective implements ObjectiveFunction<Fisher> {


    /**
     * how many days pass between one check and the other? Basically cashflow:
     * Cash(today) - Cash(today-period)
     */
    private final int period;

    public CashFlowObjective(int period) {
        this.period = period;
    }


    /**
     * compute current fitness of the agent
     *
     * @param observer
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observer, Fisher observed) {
        //get cash available today
        double currentCash = observed.getBankBalance();
        //get cash in the past (if not present, assumes it started at 0)
        double laggedCash = getCashInPast(observed, period);

        return currentCash - laggedCash;
    }

    private double getCashInPast(Fisher observed, int daysAgo) {


        return observed.getDailyData().numberOfObservations() > daysAgo ?
            observed.balanceXDaysAgo(daysAgo) : 0;
    }


}

