package uk.ac.ox.oxfish.model;

import uk.ac.ox.oxfish.YamlMain;
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
     * number of years to run each model
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
     * nullable path towards policy
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


    public BatchRunner(
            Path yamlFile, int yearsToRun, List<String> columnsToPrint,
            Path outputFolder, Path policyFile, long initialSeed) {
        this.yamlFile = yamlFile;
        this.initialSeed = initialSeed;
        this.yearsToRun = yearsToRun;
        this.columnsToPrint = new LinkedList<>();
        for(String column : columnsToPrint)
            this.columnsToPrint.add(column.trim());
        this.outputFolder = outputFolder;
        this.policyFile = policyFile;

    }



    public void run() throws IOException {
        YamlMain main = new YamlMain();
        main.setSeed(initialSeed+runsDone);
        main.setAdditionalData(true);
        if(policyFile!=null)
            main.setPolicyScript(policyFile.toString());
        main.setYearsToRun(yearsToRun);

        String simulationName = yamlFile.getFileName().toString().split("\\.")[0]+"_"+runsDone;
        FishState model = main.run(simulationName, getYamlFile(),
                                   getOutputFolder().resolve(simulationName));


        ArrayList<DataColumn> columns = new ArrayList<>();
        for(String column : columnsToPrint)
            columns.add(model.getYearlyDataSet().getColumn(column));


        FishStateUtilities.printCSVColumnsToFile(
                outputFolder.resolve("run"+runsDone+".csv").toFile(),
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
