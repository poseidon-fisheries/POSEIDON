package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

public class EnvironmentalMapFactory implements AlgorithmFactory<AdditionalStartable> {

    private final int mapPeriod = 365;
    private String mapVariableName;
    private InputPath gridFile;
    private DoubleParameter threshold;
    private DoubleParameter penalty;

    public EnvironmentalMapFactory() {
    }

    public EnvironmentalMapFactory(
        final String mapVariableName,
        final InputPath gridFile,
        final DoubleParameter threshold,
        final DoubleParameter penalty
    ) {
        this.mapVariableName = mapVariableName;
        this.gridFile = gridFile;
        this.threshold = threshold;
        this.penalty = penalty;
    }

    public DoubleParameter getThreshold() {
        return threshold;
    }

    public void setThreshold(final DoubleParameter threshold) {
        this.threshold = threshold;
    }

    public DoubleParameter getPenalty() {
        return penalty;
    }

    public void setPenalty(final DoubleParameter penalty) {
        this.penalty = penalty;
    }

    public String getMapVariableName() {
        return mapVariableName;
    }

    public void setMapVariableName(final String mapVariableName) {
        this.mapVariableName = mapVariableName;
    }

    public InputPath getGridFile() {
        return gridFile;
    }

    public void setGridFile(final InputPath gridFile) {
        this.gridFile = gridFile;
    }

    public int getMapPeriod() {
        return mapPeriod;
    }

    @Override
    public AdditionalStartable apply(final FishState fishState) {
        return new AdditionalMapFactory(mapVariableName, gridFile, mapPeriod).apply(fishState);
    }
}
