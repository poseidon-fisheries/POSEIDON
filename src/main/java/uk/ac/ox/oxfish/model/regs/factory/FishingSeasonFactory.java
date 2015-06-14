package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.utility.StrategyFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates fishing seasons of any amount
 */
public class FishingSeasonFactory implements StrategyFactory<FishingSeason>
{

    /**
     * number of days one is allowed to fish. It gets rounded
     */
    private DoubleParameter seasonLength = new FixedDoubleParameter(200);


    public FishingSeasonFactory() {
    }

    public FishingSeasonFactory(double seasonLength, boolean respectMPA) {
        this.seasonLength =new FixedDoubleParameter(seasonLength);
        this.respectMPA = respectMPA;
    }

    /**
     * is the mpa to be respected?
     */
    private boolean respectMPA = true;


    /**
     * creates a fishing season regulation for this agent
     */
    @Override
    public FishingSeason apply(FishState state) {
        final int length = seasonLength.apply(state.random).intValue();
        return new FishingSeason(respectMPA,length);
    }

    public DoubleParameter getSeasonLength() {
        return seasonLength;
    }

    public void setSeasonLength(DoubleParameter seasonLength) {
        this.seasonLength = seasonLength;
    }

    public boolean isRespectMPA() {
        return respectMPA;
    }

    public void setRespectMPA(boolean respectMPA) {
        this.respectMPA = respectMPA;
    }
}
