package uk.ac.ox.oxfish.model.plugins;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.IntegerParameter;

public class EnvironmentalMapFactory implements AlgorithmFactory<AdditionalStartable> {

    private IntegerParameter mapPeriod;
    private String mapVariableName;
    private InputPath gridFile;
    private DoubleParameter target;
    private DoubleParameter penalty;
    private DoubleParameter margin;
    private DoubleParameter target2;
    private DoubleParameter target3;
    private int nTargets =1;

    public EnvironmentalMapFactory() {
    }

    public EnvironmentalMapFactory(
        final String mapVariableName,
        final InputPath gridFile,
        final IntegerParameter mapPeriod,
        final DoubleParameter target,
        final DoubleParameter penalty,
        final DoubleParameter margin
    ) {
        this.mapVariableName = mapVariableName;
        this.gridFile = gridFile;
        this.mapPeriod = mapPeriod;
        this.margin = margin;
        this.penalty = penalty;
        this.target = target;
        nTargets=1;
//        this.target2 = null;
//        this.target3 = null;
    }
    public EnvironmentalMapFactory(
        final String mapVariableName,
        final InputPath gridFile,
        final IntegerParameter mapPeriod,
        final DoubleParameter target,
        final DoubleParameter penalty,
        final DoubleParameter margin,
        final DoubleParameter target2,
        final DoubleParameter target3
    ) {
        this.mapVariableName = mapVariableName;
        this.gridFile = gridFile;
        this.mapPeriod = mapPeriod;
        this.margin = margin;
        this.penalty = penalty;
        this.target = target;
        this.target2 = target2;
        this.target3 = target3;
        this.nTargets=3;
    }

    public DoubleParameter getTarget() {
        return target;
    }
    public DoubleParameter getTarget2(){
        return target2;
    }
    public DoubleParameter getTarget3(){
        return target3;
    }

    public DoubleParameter getTarget(int i){
        if(nTargets==1){
            return target;
        } else {
            if(i%3==0) return target;
            else if(i%3==1) return target2;
            else return target3;
        }
    }

    public void setTarget(final DoubleParameter[] target) {
        this.target = target[0];
        if(target.length>=2) this.target2 = target[1];
        if(target.length>=3) this.target3 = target[2];
    }

    public void setTarget(final DoubleParameter target) {
        this.target = target;
    }

    public void setTarget2(final DoubleParameter target2){
        this.target2 = target2;
    }
    public void setTarget3(final DoubleParameter target3){
        this.target3 = target3;
    }

    public DoubleParameter getMargin() {
        return margin;
    }

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

    public IntegerParameter getMapPeriod() {
        return mapPeriod;
    }

    public void setMapPeriod(final IntegerParameter mapPeriod) {
        this.mapPeriod = mapPeriod;
    }

    @Override
    public AdditionalStartable apply(final FishState fishState) {
        return new AdditionalMapFactory(mapVariableName, gridFile, mapPeriod.getValue()).apply(fishState);
    }
}
