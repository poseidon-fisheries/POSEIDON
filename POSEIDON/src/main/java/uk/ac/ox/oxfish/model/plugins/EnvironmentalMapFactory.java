package uk.ac.ox.oxfish.model.plugins;

import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.util.function.DoubleBinaryOperator;

public class EnvironmentalMapFactory implements AlgorithmFactory<AdditionalStartable> {

    private final int mapPeriod = 365;
    private String mapVariableName;
    private InputPath gridFile;
    private DoubleParameter target;
    private DoubleParameter penalty;
    private DoubleParameter margin;

    public EnvironmentalMapFactory() {
    }

    public EnvironmentalMapFactory(
        final String mapVariableName,
        final InputPath gridFile,
        final DoubleParameter target,
        final DoubleParameter penalty,
        final DoubleParameter margin
    ) {
        this.mapVariableName = mapVariableName;
        this.gridFile = gridFile;
        this.margin = margin;
        this.penalty = penalty;
        this.target = target;
    }

    public DoubleParameter getTarget() { return target;}

    public void setTarget(final DoubleParameter target) {
        this.target = target;
    }

    public DoubleParameter getMargin() { return margin;}

    public void setMargin(final DoubleParameter margin) {
        this.margin = margin;
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
