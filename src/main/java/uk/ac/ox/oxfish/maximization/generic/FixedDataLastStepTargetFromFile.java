package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FixedDataLastStepTargetFromFile implements DataTarget {


    private String pathToCsvFile;

    private final FixedDataLastStepTarget delegate = new FixedDataLastStepTarget();


    @Override
    public double computeError(FishState model) {


        try {
            List<String> strings = Files.readAllLines(Paths.get(pathToCsvFile));
            delegate.setFixedTarget(Double.parseDouble(
                    strings.get(strings.size()-1)));

        } catch (IOException e) {

            throw new RuntimeException("can't read " + pathToCsvFile +" because of " + e);
        }
        return delegate.computeError(model);
    }

    public FixedDataLastStepTargetFromFile() {
    }


    public String getPathToCsvFile() {
        return pathToCsvFile;
    }

    public void setPathToCsvFile(String pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
    }

    public String getYearlyDataColumnName() {
        return delegate.getColumnName();
    }

    public void setYearlyDataColumnName(String columnName) {
        delegate.setColumnName(columnName);
    }

    public double getExponent() {
        return delegate.getExponent();
    }

    public void setExponent(double exponent) {
        delegate.setExponent(exponent);
    }
}
