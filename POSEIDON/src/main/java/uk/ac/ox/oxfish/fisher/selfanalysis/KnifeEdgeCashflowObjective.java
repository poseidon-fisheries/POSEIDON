/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * The knife-edge version of cashflow objective. +1 utility if the cashflow changed by threshold or more, -1 otherwise
 * Created by carrknight on 1/28/17.
 */
public class KnifeEdgeCashflowObjective implements  ObjectiveFunction<Fisher>{


    private final double threshold;

    private final CashFlowObjective delegate;


    public KnifeEdgeCashflowObjective(double threshold, CashFlowObjective delegate) {
        this.threshold = threshold;
        this.delegate = delegate;
    }

    /**
     * compute current fitness of the agent
     *
     *
     * @param observer
     * @param observed agent whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    @Override
    public double computeCurrentFitness(Fisher observer, Fisher observed) {
        return delegate.computeCurrentFitness(observer, observed) >= threshold ? +1 : -1;
    }




}
