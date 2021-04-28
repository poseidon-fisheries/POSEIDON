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

package uk.ac.ox.oxfish.model;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.Pair;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by carrknight on 9/24/16.
 */
public class BatchRunner
{


    /**
     * where is the scenario file?
     */
    private final Path yamlFile;

    /**
     * number of years to runs each model
     */
    private final int yearsToRun;


    /**
     * list of data columns to print
     */
    private final List<String> columnsToPrint;

    /**
     * where to print output
     */
    @Nullable
    private final Path outputFolder;

    /**
     * nullable osmoseWFSPath towards policy
     */
    private final Path policyFile;

    /**
     * random seed
     */
    private final long initialSeed;

    /**
     * the number of runs
     */
    private int runsDone = 0;

    private final Integer heatmapGathererStartYear;

    /**
     * this is an helper for anything else that we need to do to a scenario
     */
    private Consumer<Scenario>  scenarioSetup;

    /**
     * this is a hook for anything you want to do to FishState after the scenario is loaded but BEFORE start is called
     */
    private Consumer<FishState> beforeStartSetup;


    /**
     * function to add columns between the year-run columns and the data columns from the model itself.
     */
    private ColumnModifier columnModifier;

    /**
     * this get called once a year and can stop a simulation for running for too long
     */
    private List<Predicate<FishState>> modelInterruptors = new LinkedList();

    //the problem with adding plugins through scenario is that they may screw up the seed as the stack has to randomize it
    //the solution then is simply not to start anything until the right year arrives. This will make the seed
    //still inconsistent after the startable... starts, but at least until then it's okay
    private             LinkedList<Pair<Integer,
            AlgorithmFactory<? extends AdditionalStartable>>>  outsidePlugins = new LinkedList<>();

    public BatchRunner(
            Path yamlFile, int yearsToRun,@Nullable List<String> columnsToPrint,
            @Nullable Path outputFolder, Path policyFile, long initialSeed,
            Integer heatmapGathererStartYear) {
        this.yamlFile = yamlFile;
        this.initialSeed = initialSeed;
        this.yearsToRun = yearsToRun;
        this.columnsToPrint = new LinkedList<>();
        if(columnsToPrint!=null) {
            for (String column : columnsToPrint)
                this.columnsToPrint.add(column.trim());
        }
        this.outputFolder = outputFolder;
        this.policyFile = policyFile;
        this.heatmapGathererStartYear = heatmapGathererStartYear;
    }

    private boolean scaleSeedWithRunsDone = true;

    public boolean isScaleSeedWithRunsDone() {
        return scaleSeedWithRunsDone;
    }

    public void setScaleSeedWithRunsDone(boolean scaleSeedWithRunsDone) {
        this.scaleSeedWithRunsDone = scaleSeedWithRunsDone;
    }

    public StringBuffer run(@Nullable StringBuffer writer) throws IOException {


        String simulationName = guessSimulationName() +"_"+runsDone;


        long startTime = System.currentTimeMillis();
        FishState model = FishStateUtilities.run(simulationName, getYamlFile(),
                outputFolder == null ? null : getOutputFolder().resolve(simulationName),
                scaleSeedWithRunsDone ? initialSeed + runsDone : initialSeed,
                Log.LEVEL_INFO,
                true, policyFile == null ?
                        null : policyFile.toString(),
                yearsToRun, false,
                heatmapGathererStartYear,
                getScenarioSetup(),
                beforeStartSetup,
                outsidePlugins,
                modelInterruptors
        );
        System.out.println("Run took: " + (System.currentTimeMillis()-startTime)/1000 + " seconds");

        //print individually
        ArrayList<DataColumn> columns = new ArrayList<>();
        if(columnsToPrint==null | columnsToPrint.isEmpty())
            for (DataColumn column : model.getYearlyDataSet().getColumns()) {
                columnsToPrint.add(column.getName());
            }

        System.out.println(columnsToPrint);
        for(String column : columnsToPrint) {
            DataColumn columnToPrint = model.getYearlyDataSet().getColumn(column);

            if(columnToPrint!=null) {
                Preconditions.checkState(columnToPrint != null, "Can't find column " + column);
                columns.add(columnToPrint);
            }
        }


        if(outputFolder!=null) {
            FishStateUtilities.printCSVColumnsToFile(
                    outputFolder.resolve(simulationName + "_run" + runsDone + ".csv").toFile(),
                    columns.toArray(new DataColumn[columns.size()])
            );
        }
        //print it tidyly if needed
        int yearsActuallyRan = model.getYearlyDataSet().numberOfObservations();
        if(writer!=null)
            for(DataColumn column : columns)
                for(int year=0; year<yearsActuallyRan; year++) {
                    writer.append(runsDone).append(",").append(year).append(",");
                    if(columnModifier!=null)
                        columnModifier.consume(writer,
                                model,
                                year);

                    writer.append(column.getName()).append(
                            ",").append(column.get(year)).append("\n");

                }
        //if needed, push out also
        outputDailyDataToWrite(model);

        //new run
        runsDone++;
        model.finish();
        return writer;

    }


    private StringBuffer tidyDailyDataWriter;

    private List<String> dailyColumnsToPrint = new LinkedList<>();


    private void outputDailyDataToWrite(FishState model){
        //don't bother if there isn't anything to write
        if(tidyDailyDataWriter == null || dailyColumnsToPrint==null || dailyColumnsToPrint.isEmpty())
            return;

        //columns!
        LinkedList<DataColumn> columns = new LinkedList<>();
        System.out.println(dailyColumnsToPrint);
        for(String column : dailyColumnsToPrint) {
            DataColumn columnToPrint = model.getDailyDataSet().getColumn(column);

            if(columnToPrint!=null) {
                Preconditions.checkState(columnToPrint != null,
                        "Can't find column " + column);
                columns.add(columnToPrint);
            }
        }

        int daysSimulated = model.getDailyDataSet().numberOfObservations();
        for(DataColumn column : columns)
            for(int day=0; day<daysSimulated; day++) {
                tidyDailyDataWriter.append(runsDone).append(",").append(day).append(",");
                if(columnModifier!=null)
                    columnModifier.consume(tidyDailyDataWriter,
                            model,
                            day);
                tidyDailyDataWriter.append(column.getName()).append(
                        ",").append(column.get(day)).append("\n");

            }



    }

    public StringBuffer getTidyDailyDataWriter() {
        return tidyDailyDataWriter;
    }

    public void setTidyDailyDataWriter(StringBuffer tidyDailyDataWriter) {
        this.tidyDailyDataWriter = tidyDailyDataWriter;
    }

    public List<String> getDailyColumnsToPrint() {
        return dailyColumnsToPrint;
    }

    public void setDailyColumnsToPrint(List<String> dailyColumnsToPrint) {
        this.dailyColumnsToPrint = dailyColumnsToPrint;
    }

    public String guessSimulationName() {
        return yamlFile.getFileName().toString().split("\\.")[0];
    }


    public Path getFolderWhereSingleFilesAreDumped(){
        return getOutputFolder().resolve(guessSimulationName());
    }

    /**
     * Getter for property 'yamlFile'.
     *
     * @return Value for property 'yamlFile'.
     */
    public Path getYamlFile() {
        return yamlFile;
    }

    /**
     * Getter for property 'yearsToRun'.
     *
     * @return Value for property 'yearsToRun'.
     */
    public int getYearsToRun() {
        return yearsToRun;
    }

    /**
     * Getter for property 'columnsToPrint'.
     *
     * @return Value for property 'columnsToPrint'.
     */
    public List<String> getColumnsToPrint() {
        return columnsToPrint;
    }


    /**
     * Getter for property 'outputFolder'.
     *
     * @return Value for property 'outputFolder'.
     */
    public Path getOutputFolder() {
        return outputFolder;
    }

    /**
     * Getter for property 'policyFile'.
     *
     * @return Value for property 'policyFile'.
     */
    public Path getPolicyFile() {
        return policyFile;
    }

    /**
     * Getter for property 'runsDone'.
     *
     * @return Value for property 'runsDone'.
     */
    public int getRunsDone() {
        return runsDone;
    }

    /**
     * Getter for property 'scenarioSetup'.
     *
     * @return Value for property 'scenarioSetup'.
     */
    public Consumer<Scenario> getScenarioSetup() {
        return scenarioSetup;
    }

    /**
     * Setter for property 'scenarioSetup'.
     *
     * @param scenarioSetup Value to set for property 'scenarioSetup'.
     */
    public void setScenarioSetup(Consumer<Scenario> scenarioSetup) {
        this.scenarioSetup = scenarioSetup;
    }


    /**
     * Getter for property 'columnModifier'.
     *
     * @return Value for property 'columnModifier'.
     */
    public ColumnModifier getColumnModifier() {
        return columnModifier;
    }

    /**
     * Setter for property 'columnModifier'.
     *
     * @param columnModifier Value to set for property 'columnModifier'.
     */
    public void setColumnModifier(ColumnModifier columnModifier) {
        this.columnModifier = columnModifier;
    }

    static public interface ColumnModifier{

        void consume(StringBuffer writer, FishState model, Integer year);

    }


    public Consumer<FishState> getBeforeStartSetup() {
        return beforeStartSetup;
    }

    public void setBeforeStartSetup(Consumer<FishState> beforeStartSetup) {
        this.beforeStartSetup = beforeStartSetup;
    }

    public List<Predicate<FishState>> getModelInterruptors() {
        return modelInterruptors;
    }

    public void setModelInterruptors(List<Predicate<FishState>> modelInterruptors) {
        this.modelInterruptors = modelInterruptors;
    }

    public             LinkedList<Pair<Integer,
            AlgorithmFactory<? extends AdditionalStartable>>>  getOutsidePlugins() {
        return outsidePlugins;
    }

    public void setOutsidePlugins(            LinkedList<Pair<Integer,
            AlgorithmFactory<? extends AdditionalStartable>>>  outsidePlugins) {
        this.outsidePlugins = outsidePlugins;
    }
}
