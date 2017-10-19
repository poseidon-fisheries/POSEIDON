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

package uk.ac.ox.oxfish.experiments;

import sim.field.grid.IntGrid2D;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Demo to check the california fuel expenditures against "real" data
 * Created by carrknight on 3/22/16.
 */
public class CaliforniaFuelExpenditures {

    public static final Path MAIN_DIRECTORY = Paths.get("runs", "califuel");

    public static void main(String[] args) throws IOException {

        MAIN_DIRECTORY.toFile().mkdirs();

        CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();


        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);

        state.start();

        while(state.getYear()<5)
        {
            state.schedule.step(state);
            IntGrid2D trawls = state.getMap().getDailyTrawlsMap();
        }

        state.schedule.step(state);

        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), MAIN_DIRECTORY.resolve("fuel.csv").toFile(),
                fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.FUEL_EXPENDITURE)

        );
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), MAIN_DIRECTORY.resolve("trips.csv").toFile(),
                fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIPS)

        );
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), MAIN_DIRECTORY.resolve("effort.csv").toFile(),
                fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.EFFORT)

        );
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), MAIN_DIRECTORY.resolve("distance.csv").toFile(),
                fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.FISHING_DISTANCE)

        );
        FishStateUtilities.pollHistogramToFile(
                state.getFishers(), MAIN_DIRECTORY.resolve("duration.csv").toFile(),
                fisher -> fisher.getLatestYearlyObservation(FisherYearlyTimeSeries.TRIP_DURATION)

        );

    }

}
