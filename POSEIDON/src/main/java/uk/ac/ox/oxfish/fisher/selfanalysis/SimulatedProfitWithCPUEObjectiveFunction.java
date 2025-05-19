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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import com.google.common.annotations.VisibleForTesting;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;

/**
 * rather than looking at previous profits, this function uses a simulator to produce a new trip with new profits
 * taking last CPUE observed on trip record as expected constant CPUE for future trip.
 * From the simulated trip it then extract and return its hourly profits
 */
public class SimulatedProfitWithCPUEObjectiveFunction extends TripBasedObjectiveFunction {

    private final int maxHoursOut;

    private ProfitFunction simulator;

    public SimulatedProfitWithCPUEObjectiveFunction(int maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
        this.simulator = new ProfitFunction(maxHoursOut);
    }

    @VisibleForTesting
    public SimulatedProfitWithCPUEObjectiveFunction(int maxHoursOut, ProfitFunction simulator) {
        this.maxHoursOut = maxHoursOut;
        this.simulator = simulator;
    }

    @Override
    protected double extractUtilityFromTrip(Fisher observer, TripRecord tripRecord, Fisher observed) {
        //if there is no trip record to copy or no tile to look at, just return NaN
        if (tripRecord == null)
            return Double.NaN;
        SeaTile tile = tripRecord.getMostFishedTileInTrip();
        if (tile == null)
            return Double.NaN;

        //get CPUE
        //todo remove this and use the last hour CPUE rather than the average?
        double[] cpue = tripRecord.getTotalCPUE();


        //if it's our own trip, just quickly call predict
        if (observer == observed || observed == null || observed.getGear().isSame(observer.getGear())) {
            return simulator.hourlyProfitFromHypotheticalTripHere(observer, tile, observed.grabState(),
                cpue, false
            );
        }

        //if it's not your trip, you need to weigh the efficiency of your gear compared to the one you are observing
        double[] expectedCPUE = new double[cpue.length];
        double[] myEfficiency =
            observer.getGear().expectedHourlyCatch(
                observer,
                tile,
                1,
                observer.grabState().getBiology()
            );
        double[] theirEfficiency =
            observed.getGear().expectedHourlyCatch(
                observed,
                tile,
                1,
                observer.grabState().getBiology()
            );

        for (int i = 0; i < expectedCPUE.length; i++) {
            expectedCPUE[i] = cpue[i] * (theirEfficiency[i] == 0 ? 0 : myEfficiency[i] / theirEfficiency[i]);
        }

        return simulator.hourlyProfitFromHypotheticalTripHere(observer, tile, observed.grabState(),
            expectedCPUE, false
        );
    }


}
