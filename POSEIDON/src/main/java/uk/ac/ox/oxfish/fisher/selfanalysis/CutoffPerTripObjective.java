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

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * Takes hourly profit objective function but adds potentially two thresholds above and below
 * Created by carrknight on 1/28/17.
 */
public class CutoffPerTripObjective implements ObjectiveFunction<Fisher> {


    private final HourlyProfitInTripObjective delegate;


    private final double lowThreshold;

    private final double highThreshold;

    public CutoffPerTripObjective(
        HourlyProfitInTripObjective delegate,
        double lowThreshold,
        double highThreshold
    ) {
        if (Double.isFinite(lowThreshold) && Double.isFinite(highThreshold))
            Preconditions.checkArgument(lowThreshold <= highThreshold);
        this.delegate = delegate;
        this.lowThreshold = lowThreshold;
        this.highThreshold = highThreshold;
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
        return censor(delegate.computeCurrentFitness(observer, observed));
    }

    private double censor(double profit) {
        if (Double.isFinite(lowThreshold) && profit <= lowThreshold)
            return lowThreshold;
        if (Double.isFinite(highThreshold) && profit >= highThreshold)
            return highThreshold;
        return profit;
    }

}
