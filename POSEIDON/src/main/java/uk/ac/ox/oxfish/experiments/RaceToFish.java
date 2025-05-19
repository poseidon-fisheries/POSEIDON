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

package uk.ac.ox.oxfish.experiments;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.ITQMonoFactory;
import uk.ac.ox.oxfish.model.regs.factory.TACMonoFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

/**
 * Created by carrknight on 1/12/16.
 */
public class RaceToFish {


    public static final String EFFORT_COLUMN_NAME = "Yearly Effort In Months";
    public static final int NUMBER_OF_RUNS = 50;
    public static final Path INPUT_FOLDER = Paths.get("docs", "20160113 race");
    public static final Path OUTPUT_FOLDER = Paths.get("docs", "20160113 race", "runs");
    public static final int QUOTA_VALUE = 5000;

    public static void main(String[] args) throws IOException {

        OUTPUT_FOLDER.toFile().mkdirs();
        policySweepRaceToFish("race", INPUT_FOLDER, NUMBER_OF_RUNS, OUTPUT_FOLDER, EFFORT_COLUMN_NAME, QUOTA_VALUE);
        // policySweepRaceToFish("corner");
    }


    public static void policySweepRaceToFish(
        final String scenarioFileName,
        final Path inputFolder,
        final int numberOfRuns, final Path outputFolder, final String effortColumnName, final int quotaValue
    ) throws IOException {

        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
            inputFolder.resolve(scenarioFileName + ".yaml")));

        System.out.println("Running Corner Case With No Rules");
        for (int run = 0; run < numberOfRuns; run++) {
            System.out.println("lspiRun " + run);
            PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
            System.out.println(scenario.getDepartingStrategy());
            scenario.setRegulation(new AnarchyFactory());
            FishState state = new FishState(run);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);
            //done!
            File dailyFile = outputFolder.resolve(scenarioFileName + "_anarchy_daily_" + run + ".csv").toFile();
            FishStateUtilities.printCSVColumnsToFile(
                dailyFile,
                state.getDailyDataSet().getColumn("Price of Species 0 at Port 0"),
                state.getDailyDataSet().getColumn("Fishers at Sea")
            );
            File yearlyFile = outputFolder.resolve(scenarioFileName + "_anarchy_yearly_" + run + ".csv").toFile();
            LinkedList<DataColumn> columns = new LinkedList<>();
            assert state.getYearlyDataSet().getColumn("Average Cash-Flow") != null;
            assert state.getYearlyDataSet().getColumn(effortColumnName) != null;
            columns.add(state.getYearlyDataSet().getColumn(effortColumnName));
            for (int i = 0; i < 12; i++) {
                DataColumn column = state.getYearlyDataSet().getColumn("Yearly Efforts In Month " + i);
                assert column != null;
                columns.add(column);
            }
            columns.add(state.getYearlyDataSet().getColumn("Average Cash-Flow"));

            FishStateUtilities.printCSVColumnsToFile(yearlyFile, columns.toArray(new DataColumn[columns.size()]));
        }

        System.out.println("Running Corner Case With TAC");
        for (int run = 0; run < numberOfRuns; run++) {
            System.out.println("lspiRun " + run);
            PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
            TACMonoFactory tac = new TACMonoFactory();
            tac.setQuota(new FixedDoubleParameter(quotaValue * 100));
            scenario.setRegulation(tac);
            FishState state = new FishState(run);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);
            //done!
            File dailyFile = outputFolder.resolve(scenarioFileName + "_tac_daily_" + run + ".csv").toFile();
            FishStateUtilities.printCSVColumnsToFile(
                dailyFile,
                state.getDailyDataSet().getColumn("Price of Species 0 at Port 0"),
                state.getDailyDataSet().getColumn("Fishers at Sea")
            );
            File yearlyFile = outputFolder.resolve(scenarioFileName + "_tac_yearly_" + run + ".csv").toFile();
            LinkedList<DataColumn> columns = new LinkedList<>();
            columns.add(state.getYearlyDataSet().getColumn(effortColumnName));
            for (int i = 0; i < 12; i++)
                columns.add(state.getYearlyDataSet().getColumn("Yearly Efforts In Month " + i));
            columns.add(state.getYearlyDataSet().getColumn("Average Cash-Flow"));

            FishStateUtilities.printCSVColumnsToFile(yearlyFile, columns.toArray(new DataColumn[columns.size()]));
        }


        System.out.println("Running Corner Case With ITQ");
        for (int run = 0; run < numberOfRuns; run++) {
            System.out.println("lspiRun " + run);
            PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
            ITQMonoFactory tac = new ITQMonoFactory();
            tac.setIndividualQuota(new FixedDoubleParameter(quotaValue));
            scenario.setRegulation(tac);
            FishState state = new FishState(run);
            state.setScenario(scenario);
            state.start();
            while (state.getYear() < 20)
                state.schedule.step(state);
            //done!
            File dailyFile = outputFolder.resolve(scenarioFileName + "_itq_daily_" + run + ".csv").toFile();
            FishStateUtilities.printCSVColumnsToFile(
                dailyFile,
                state.getDailyDataSet().getColumn("Price of Species 0 at Port 0"),
                state.getDailyDataSet().getColumn("Fishers at Sea")
            );
            File yearlyFile = outputFolder.resolve(scenarioFileName + "_itq_yearly_" + run + ".csv").toFile();
            LinkedList<DataColumn> columns = new LinkedList<>();
            columns.add(state.getYearlyDataSet().getColumn(effortColumnName));
            for (int i = 0; i < 12; i++)
                columns.add(state.getYearlyDataSet().getColumn("Yearly Efforts In Month " + i));
            columns.add(state.getYearlyDataSet().getColumn("Average Cash-Flow"));

            FishStateUtilities.printCSVColumnsToFile(yearlyFile, columns.toArray(new DataColumn[columns.size()]));
        }

    }
}
