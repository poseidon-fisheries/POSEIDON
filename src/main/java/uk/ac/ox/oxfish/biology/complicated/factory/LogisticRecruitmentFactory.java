package uk.ac.ox.oxfish.biology.complicated.factory;

import uk.ac.ox.oxfish.biology.complicated.LogisticRecruitmentProcess;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Created by carrknight on 7/8/17.
 */
public class LogisticRecruitmentFactory implements AlgorithmFactory<LogisticRecruitmentProcess>
{

    private DoubleParameter carryingCapacity = new FixedDoubleParameter(100000000);

    private DoubleParameter malthusianParameter = new FixedDoubleParameter(.6);


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public LogisticRecruitmentProcess apply(FishState fishState) {
        return new LogisticRecruitmentProcess(carryingCapacity.apply(fishState.getRandom()),
                                              malthusianParameter.apply(fishState.getRandom()));
    }

    public DoubleParameter getCarryingCapacity() {
        return carryingCapacity;
    }

    public void setCarryingCapacity(DoubleParameter carryingCapacity) {
        this.carryingCapacity = carryingCapacity;
    }

    public DoubleParameter getMalthusianParameter() {
        return malthusianParameter;
    }

    public void setMalthusianParameter(DoubleParameter malthusianParameter) {
        this.malthusianParameter = malthusianParameter;
    }
}
