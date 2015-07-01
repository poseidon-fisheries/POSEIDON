package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

/**
 * Creates a separate mono-quota regulation object each time it is called. This way each quota acts independently
 * Created by carrknight on 6/14/15.
 */
public class ITQMonoFactory implements AlgorithmFactory<MonoQuotaRegulation>
{


    DoubleParameter individualQuota = new FixedDoubleParameter(1000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MonoQuotaRegulation apply(FishState state) {
        return new MonoQuotaRegulation(individualQuota.apply(state.random),state);
    }

    public DoubleParameter getIndividualQuota() {
        return individualQuota;
    }

    public void setIndividualQuota(DoubleParameter individualQuota) {
        this.individualQuota = individualQuota;
    }
}
