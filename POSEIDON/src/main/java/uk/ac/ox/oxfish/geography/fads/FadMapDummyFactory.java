package uk.ac.ox.oxfish.geography.fads;

import org.jetbrains.annotations.NotNull;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbundanceFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.currents.ConstantCurrentVector;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

public class FadMapDummyFactory implements AlgorithmFactory<FadMap>  {

    private boolean isBiomassOnly = false;



    private DoubleParameter fixedXCurrent = new FixedDoubleParameter(+1);
    private DoubleParameter fixedYCurrent = new FixedDoubleParameter(-1);

    @NotNull
    protected ConstantCurrentVector getCurrentVectors(FishState fishState, NauticalMap map) {
        return new ConstantCurrentVector(
                fixedXCurrent.apply(fishState.getRandom()),
                fixedYCurrent.apply(fishState.getRandom()),
                map.getHeight(),
                map.getWidth()
        );
    }

    public DoubleParameter getFixedXCurrent() {
        return fixedXCurrent;
    }

    public void setFixedXCurrent(DoubleParameter fixedXCurrent) {
        this.fixedXCurrent = fixedXCurrent;
    }

    public DoubleParameter getFixedYCurrent() {
        return fixedYCurrent;
    }

    public void setFixedYCurrent(DoubleParameter fixedYCurrent) {
        this.fixedYCurrent = fixedYCurrent;
    }

    @Override
    public FadMap apply(FishState fishState) {


        NauticalMap map = fishState.getMap();
        return new FadMap(
                map,
                getCurrentVectors(fishState, map),
                fishState.getBiology(),
                isBiomassOnly ? BiomassLocalBiology.class : AbundanceLocalBiology.class,
                isBiomassOnly ? BiomassFad.class : AbundanceFad.class
        );


    }


    public boolean isBiomassOnly() {
        return isBiomassOnly;
    }

    public void setBiomassOnly(boolean biomassOnly) {
        isBiomassOnly = biomassOnly;
    }
}
