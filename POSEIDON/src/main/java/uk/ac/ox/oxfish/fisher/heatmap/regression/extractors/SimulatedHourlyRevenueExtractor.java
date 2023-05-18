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

package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Use a simulator (and perfect knowledge) to check how much $/hr one would make (ignoring costs)
 * Created by carrknight on 2/13/17.
 */
public class SimulatedHourlyRevenueExtractor implements ObservationExtractor {


    private final static LameTripSimulator simulator = new LameTripSimulator();
    private final double maxHoursOut;

    public SimulatedHourlyRevenueExtractor(final double maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }


    @Override
    public double extract(
        SeaTile tile, double timeOfObservation, Fisher agent, FishState model
    ) {


        TripRecord simulation = simulator.simulateRecord(
            agent,
            tile,
            model,
            maxHoursOut,
            //using perfect knowledge!
            agent.getGear().expectedHourlyCatch(agent, tile, 1,
                model.getBiology()
            )
        );
        if (simulation == null)
            return -10000;
        return simulation.getEarnings() / simulation.getDurationInHours();
    }
}
