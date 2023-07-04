package uk.ac.ox.oxfish.maximization.generic;

import uk.ac.ox.oxfish.model.FishState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class FixedDataLastStepTargetFromFile implements DataTarget {


    private static final long serialVersionUID = 6998271990016935372L;
    private final FixedDataLastStepTarget delegate = new FixedDataLastStepTarget();
    private String pathToCsvFile;


    public FixedDataLastStepTargetFromFile() {
    }

    @Override
    public double computeError(final FishState model) {


        try {
            final List<String> strings = Files.readAllLines(Paths.get(pathToCsvFile));
            delegate.setFixedTarget(Double.parseDouble(
                strings.get(strings.size() - 1)));

        } catch (final IOException e) {

            throw new RuntimeException("can't read " + pathToCsvFile + " because of " + e);
        }
        return delegate.computeError(model);
    }

    public String getPathToCsvFile() {
        return pathToCsvFile;
    }

    public void setPathToCsvFile(final String pathToCsvFile) {
        this.pathToCsvFile = pathToCsvFile;
    }

    public String getYearlyDataColumnName() {
        return delegate.getColumnName();
    }

    public void setYearlyDataColumnName(final String columnName) {
        delegate.setColumnName(columnName);
    }

    public double getExponent() {
        return delegate.getExponent();
    }

    public void setExponent(final double exponent) {
        delegate.setExponent(exponent);
    }
}
