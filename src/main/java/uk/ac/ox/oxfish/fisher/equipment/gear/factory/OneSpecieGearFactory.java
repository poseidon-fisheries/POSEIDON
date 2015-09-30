package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.OneSpecieGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Create fixed proportion gears that apply only to one specie
 * Created by carrknight on 9/30/15.
 */

public class OneSpecieGearFactory implements AlgorithmFactory<OneSpecieGear>
{

    private int specieTargetIndex = 0;

    private DoubleParameter proportionCaught = new FixedDoubleParameter(.01);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public OneSpecieGear apply(FishState state) {
        return new OneSpecieGear(state.getSpecies().get(specieTargetIndex),
                                 proportionCaught.apply(state.getRandom()));
    }


    public int getSpecieTargetIndex() {
        return specieTargetIndex;
    }

    public void setSpecieTargetIndex(int specieTargetIndex) {
        this.specieTargetIndex = specieTargetIndex;
    }

    public DoubleParameter getProportionCaught() {
        return proportionCaught;
    }

    public void setProportionCaught(DoubleParameter proportionCaught) {
        this.proportionCaught = proportionCaught;
    }
}
