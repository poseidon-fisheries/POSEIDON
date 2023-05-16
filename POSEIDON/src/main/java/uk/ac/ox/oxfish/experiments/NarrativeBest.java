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

import com.google.common.io.Files;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightLogisticPlusClimateChangeFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.TwoPopulationsScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

/**
 * Created by carrknight on 4/5/17.
 */
public class NarrativeBest {


    private static final Path originalDirectory = Paths.get("docs", "20170403 narrative", "best_runs");
    private static final Path otherDirectory = Paths.get("docs", "20170616 narrative3");
    private static final Path mainDirectory = Paths.get("docs", "20170403 narrative", "best_runs");


    private static final Path[] fileNames = new Path[]
        {
            otherDirectory.resolve("wider").resolve("small.yaml"),
            otherDirectory.resolve("wider").resolve("balanced.yaml"),
            otherDirectory.resolve("test").resolve("example_fixed.yaml"),
            otherDirectory.resolve("test").resolve("example_fixed_2.yaml"),
            originalDirectory.resolve("itqplus_best"),
            originalDirectory.resolve("tac_best"),
            originalDirectory.resolve("itq_best"),
            originalDirectory.resolve("anarchy")
        };


    public static void main(final String[] args) throws FileNotFoundException {


        for (final Path scenarioFile : fileNames) {
            Logger.getGlobal().info("Starting " + scenarioFile);
            final FishYAML yaml = new FishYAML();
            final String name = Files.getNameWithoutExtension(scenarioFile.getFileName().toString());
            final Scenario scenario = yaml.loadAs(
                new FileReader(scenarioFile.toFile()),
                Scenario.class
            );
            final FishState state = new FishState(0L);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                otherDirectory.resolve(name + ".csv").toFile(),
                state.getYearlyDataSet().getColumn("Biomass Species 0"),
                state.getYearlyDataSet().getColumn("Species 0 Landings"),
                state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                state.getYearlyDataSet().getColumn("Small Fishers Total Income"),
                state.getYearlyDataSet().getColumn("Large Fishers Total Income"),
                state.getYearlyDataSet().getColumn("Large Fishers Species 0 Landings"),
                state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings"),
                state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings"),
                state.getYearlyDataSet().getColumn("ITQ Prices Of Species 0"),
                state.getYearlyDataSet().getColumn("ITQ Volume Of Species 0")
            );

        }

        //again but with climate change
        for (final Path file : fileNames) {
            final String name = Files.getNameWithoutExtension(file.getFileName().toString());

            Logger.getGlobal().info("Starting " + name);
            final FishYAML yaml = new FishYAML();
            final TwoPopulationsScenario scenario = yaml.loadAs(
                new FileReader(file.toFile()),
                TwoPopulationsScenario.class
            );
            final FromLeftToRightLogisticPlusClimateChangeFactory biology = new FromLeftToRightLogisticPlusClimateChangeFactory();
            biology.setClimateChangePercentageMovement(new FixedDoubleParameter(.001));
            biology.setNorthMigration(1);
            biology.setWestMigration(1);

            scenario.setBiologyInitializer(biology);
            final FishState state = new FishState(0L);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);

            FishStateUtilities.printCSVColumnsToFile(
                otherDirectory.resolve(name + "_climate.csv").toFile(),
                state.getYearlyDataSet().getColumn("Biomass Species 0"),
                state.getYearlyDataSet().getColumn("Species 0 Landings"),
                state.getYearlyDataSet().getColumn("Average Cash-Flow"),
                state.getYearlyDataSet().getColumn("Small Fishers Total Income"),
                state.getYearlyDataSet().getColumn("Large Fishers Total Income"),
                state.getYearlyDataSet().getColumn("Small Fishers Total Effort"),
                state.getYearlyDataSet().getColumn("Large Fishers Total Effort"),
                state.getYearlyDataSet().getColumn("Large Fishers Species 0 Landings"),
                state.getYearlyDataSet().getColumn("Small Fishers Species 0 Landings")
            );

        }
    }


}
