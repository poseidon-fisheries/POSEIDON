package uk.ac.ox.oxfish.experiments.indonesia;

import com.google.common.base.Preconditions;
import joptsimple.internal.Strings;
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
    private String[] addressForEachColumn;


    private Path csvPathFile;

    private boolean csvHasHeader;


    /**
     * while technically possible to create this and then use setters, you might as well call the right constructor
     */
    @Deprecated
    public ReadFromCSVOptimizationParameter() {
        csvPathFile  = null;
        csvHasHeader = false;
        addressForEachColumn = null;

    }

    public ReadFromCSVOptimizationParameter(Path csvPathFile, String[] addressForEachColumn, boolean hasHeader) throws IOException {
        this.addressForEachColumn = addressForEachColumn;
        this.csvPathFile = csvPathFile;
        this.csvHasHeader = hasHeader;


    }




    @Override
    public String parametrize(Scenario scenario, double[] inputs) {

        try {
            List<String> csvContent  = Files.readAllLines(csvPathFile);
            if(csvHasHeader)
                csvContent.remove(0);

            Preconditions.checkState(inputs.length==1);
            //normalize it 0 to 1
            double input = (inputs[0]+10d)/20d;
            //the bounds might be broken by some optimizers: bound it again
            if(input<0)
                input=0;
            if(input>1)
                input=1;

            Preconditions.checkState(input>=0);
            Preconditions.checkState(input<=1);

            int row = (int) Math.floor(input * csvContent.size());
            if(row==csvContent.size()) //rounding issue (not considering the header)
                row=row-1;

            final String[] selectedRow = csvContent.get(row).split(",");
            Preconditions.checkState(selectedRow.length == addressForEachColumn.length);
            for (int i = 0; i < selectedRow.length; i++) {
                SimpleOptimizationParameter.quickParametrize(
                        scenario,
                        Double.parseDouble(selectedRow[i]),
                        addressForEachColumn[i]
                );
            }

            return Strings.join(selectedRow,";");


        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to find csv file: " + csvPathFile);
        }






    }


    public String[] getAddressForEachColumn() {
        return addressForEachColumn;
    }

    public void setAddressForEachColumn(String[] addressForEachColumn) {
        this.addressForEachColumn = addressForEachColumn;
    }

    public Path getCsvPathFile() {
        return csvPathFile;
    }

    public void setCsvPathFile(Path csvPathFile) {
        this.csvPathFile = csvPathFile;
    }

    public boolean isCsvHasHeader() {
        return csvHasHeader;
    }

    public void setCsvHasHeader(boolean csvHasHeader) {
        this.csvHasHeader = csvHasHeader;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public String getName() {
        return Strings.join(addressForEachColumn,";");
    }
}
