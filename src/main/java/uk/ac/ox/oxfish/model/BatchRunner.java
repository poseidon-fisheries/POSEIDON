package uk.ac.ox.oxfish.model;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.model.data.collectors.DataColumn;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
     * number of years to lspiRun each model
     */
    private final int yearsToRun;

    /**
     * list of data columns to print
     */
    private final List<String> columnsToPrint;

    /**
     * where to print output
     */
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

    public BatchRunner(
            Path yamlFile, int yearsToRun, List<String> columnsToPrint,
            Path outputFolder, Path policyFile, long initialSeed,
            Integer heatmapGathererStartYear) {
        this.yamlFile = yamlFile;
        this.initialSeed = initialSeed;
        this.yearsToRun = yearsToRun;
        this.columnsToPrint = new LinkedList<>();
        for(String column : columnsToPrint)
            this.columnsToPrint.add(column.trim());
        this.outputFolder = outputFolder;
        this.policyFile = policyFile;
        this.heatmapGathererStartYear = heatmapGathererStartYear;
    }



    public void run() throws IOException {


        String simulationName = yamlFile.getFileName().toString().split("\\.")[0]+"_"+runsDone;
        FishState model = FishStateUtilities.run(simulationName, getYamlFile(),
                                                 getOutputFolder().resolve(simulationName),
                                                 initialSeed + runsDone,
                                                 Log.LEVEL_INFO,
                                                 true, policyFile == null ? null : policyFile.toString(), yearsToRun, false,
                                                 heatmapGathererStartYear);


        ArrayList<DataColumn> columns = new ArrayList<>();
        for(String column : columnsToPrint) {
            DataColumn columnToPrint = model.getYearlyDataSet().getColumn(column);
            Preconditions.checkState(columnToPrint!=null, "Can't find column " + column);
            columns.add(columnToPrint);
        }


        FishStateUtilities.printCSVColumnsToFile(
                outputFolder.resolve("lspiRun"+runsDone+".csv").toFile(),
                columns.toArray(new DataColumn[columns.size()])
        );

        runsDone++;
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
}
