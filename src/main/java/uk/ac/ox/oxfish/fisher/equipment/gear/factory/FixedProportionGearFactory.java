package uk.ac.ox.oxfish.fisher.equipment.gear.factory;

import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates fixed proportion gears
 * Created by carrknight on 9/30/15.
 */
public class FixedProportionGearFactory implements AlgorithmFactory<FixedProportionGear>
{

    /**
     * this applies to each specie
     */
    private DoubleParameter catchabilityPerHour = new FixedDoubleParameter(.01);


    public FixedProportionGearFactory() {
    }


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public FixedProportionGear apply(FishState state) {
        return new FixedProportionGear(catchabilityPerHour.apply(state.getRandom()));
    }

    public DoubleParameter getCatchabilityPerHour() {
        return catchabilityPerHour;
    }

    public void setCatchabilityPerHour(DoubleParameter catchabilityPerHour) {
        this.catchabilityPerHour = catchabilityPerHour;
    }
}
