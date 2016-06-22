package uk.ac.ox.oxfish.model.regs.factory;

import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.Map;

/**
 * Multiquota factory, including TAC Opportunity Cost manager
 * Created by carrknight on 10/20/15.
 */
public class TACMultiFactory implements AlgorithmFactory<MultiQuotaRegulation>
{

    /**
     * for each model there is only one quota object being shared
     */
    private final Map<FishState,MultiQuotaRegulation> modelQuota = new HashMap<>();


    /**
     * the quota for first species
     */
    private DoubleParameter firstSpeciesQuota = new FixedDoubleParameter(500000);

    /**
     * the quota for any other species
     */
    private DoubleParameter otherSpeciesQuota = new FixedDoubleParameter(500000);


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState state)
    {

        if(!modelQuota.containsKey(state))
            modelQuota.put(state,createInstance(state.getRandom(), state.getSpecies().size(), state));

        return modelQuota.get(state);




    }



    private MultiQuotaRegulation createInstance(MersenneTwisterFast random, int numberOfSpecies,
                                                FishState state)
    {

        double[] quotas = new double[numberOfSpecies];
        quotas[0] = firstSpeciesQuota.apply(random);

        for(int i=1; i<numberOfSpecies; i++)
        {
            quotas[i] = otherSpeciesQuota.apply(random);
        }
        MultiQuotaRegulation regulations = new MultiQuotaRegulation(quotas, state);
        //now create the opportunity costs manager
     //   TACOpportunityCostManager manager = new TACOpportunityCostManager(regulations);
    //    state.registerStartable(manager);


        return regulations;
    }

    public DoubleParameter getFirstSpeciesQuota() {
        return firstSpeciesQuota;
    }

    public void setFirstSpeciesQuota(DoubleParameter firstSpeciesQuota) {
        this.firstSpeciesQuota = firstSpeciesQuota;
    }

    public DoubleParameter getOtherSpeciesQuota() {
        return otherSpeciesQuota;
    }

    public void setOtherSpeciesQuota(DoubleParameter otherSpeciesQuota) {
        this.otherSpeciesQuota = otherSpeciesQuota;
    }
}
