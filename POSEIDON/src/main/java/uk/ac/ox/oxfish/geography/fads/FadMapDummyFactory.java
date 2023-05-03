package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.ConstantCurrentVector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadMapDummyFactory implements AlgorithmFactory<FadMap> {

    private boolean isBiomassOnly = false;


    private DoubleParameter fixedXCurrent = new FixedDoubleParameter(+1);
    private DoubleParameter fixedYCurrent = new FixedDoubleParameter(-1);

    public DoubleParameter getFixedXCurrent() {
        return fixedXCurrent;
    }

    public void setFixedXCurrent(final DoubleParameter fixedXCurrent) {
        this.fixedXCurrent = fixedXCurrent;
    }

    public DoubleParameter getFixedYCurrent() {
        return fixedYCurrent;
    }

    public void setFixedYCurrent(final DoubleParameter fixedYCurrent) {
        this.fixedYCurrent = fixedYCurrent;
    }

    @Override
    public FadMap apply(final FishState fishState) {


        final NauticalMap map = fishState.getMap();
        return new FadMap(
            map,
            getCurrentVectors(fishState, map),
            fishState.getBiology(),
            isBiomassOnly ? BiomassLocalBiology.class : AbundanceLocalBiology.class
        );


    }

    @NotNull
    protected ConstantCurrentVector getCurrentVectors(final FishState fishState, final NauticalMap map) {
        return new ConstantCurrentVector(
            fixedXCurrent.applyAsDouble(fishState.getRandom()),
            fixedYCurrent.applyAsDouble(fishState.getRandom()),
            map.getHeight(),
            map.getWidth()
        );
    }

    public boolean isBiomassOnly() {
        return isBiomassOnly;
    }

    public void setBiomassOnly(final boolean biomassOnly) {
        isBiomassOnly = biomassOnly;
    }
}
