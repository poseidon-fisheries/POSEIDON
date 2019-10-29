package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.maximization.generic.OptimizationParameter;
import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;
import uk.ac.ox.oxfish.model.scenario.Scenario;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * the idea here is to read a CSV file and apply each row to a specific parameter
 */
public class ReadFromCSVOptimizationParameter implements OptimizationParameter {


    /**
     * scenario address, one for each column read!
     */
    private final String[] addressForEachColumn;



    private final List<String> csvContent;

    public ReadFromCSVOptimizationParameter(Path csvPathFile, String[] addressForEachColumn, boolean hasHeader) throws IOException {
        this.addressForEachColumn = addressForEachColumn;

        csvContent = Files.readAllLines(csvPathFile);
        if(hasHeader)
            csvContent.remove(0);
    }


    public ReadFromCSVOptimizationParameter(List<String> csvContent,String[] addressForEachColumn) throws IOException {

        this.addressForEachColumn =addressForEachColumn;
        this.csvContent = csvContent;
    }


    @Override
    public double parametrize(Scenario scenario, double[] inputs) {
        Preconditions.checkState(inputs.length==1);
        //normalize it 0 to 1
        double input = (inputs[0]+10d)/20d;
        Preconditions.checkState(input>=0);
        Preconditions.checkState(input<=1);

        int row = (int) Math.round(input * csvContent.size());

        final String[] selectedRow = csvContent.get(row).split(",");
        Preconditions.checkState(selectedRow.length == addressForEachColumn.length);
        for (int i = 0; i < selectedRow.length; i++) {
            SimpleOptimizationParameter.quickParametrize(
                    scenario,
                    Double.parseDouble(selectedRow[i]),
                    addressForEachColumn[i]
            );
        }

        return row;



    }

    @Override
    public int size() {
        return 1;
    }
}
