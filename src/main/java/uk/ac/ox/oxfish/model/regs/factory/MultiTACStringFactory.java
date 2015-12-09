package uk.ac.ox.oxfish.model.regs.factory;


import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.TACOpportunityCostManager;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.YamlConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MultiTACStringFactory implements AlgorithmFactory<MultiQuotaRegulation> {


    /**
     * The string we are going to turn into rule, "0:100 ,2:uniform 1 100" means that EACH FISHER gets 100 quotas a year
     * for species 0 and a random quota of 1 to 100 for species 2. The other species are then assumed NOT TO BE PROTECTED
     * by the quota (and can be fished out freely)
     */
    private String yearlyQuotaMaps = "0:500000";


    /**
     * for each model there is only one quota object being shared
     */
    private final Map<FishState,MultiQuotaRegulation> modelQuota = new HashMap<>();


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

        Map<String, String> quotasInputted = Splitter.on(",").withKeyValueSeparator(":").split(yearlyQuotaMaps.trim());
        Preconditions.checkArgument(quotasInputted.size() > 0, "You provided no quota for the TAC!");

        //here we store the quotas
        double[] quotas = new double[numberOfSpecies];
        //start them as non-binding
        Arrays.fill(quotas, Double.POSITIVE_INFINITY);
        //go for each input and read the results
        for(Map.Entry<String,String> input : quotasInputted.entrySet())
        {
            Double yearlyQuota = YamlConstructor.parseDoubleParameter(input.getValue().trim()).apply(state.getRandom());
            Preconditions.checkArgument(yearlyQuota>0);
            Preconditions.checkArgument(!yearlyQuota.isNaN());
            quotas[Integer.parseInt(input.getKey().trim())] = yearlyQuota;
        }

        MultiQuotaRegulation regulations = new MultiQuotaRegulation(quotas, state);
        //now create the opportunity costs manager
        TACOpportunityCostManager manager = new TACOpportunityCostManager(regulations);
        state.registerStartable(manager);

        return regulations;
    }

    public String getYearlyQuotaMaps() {
        return yearlyQuotaMaps;
    }

    public void setYearlyQuotaMaps(String yearlyQuotaMaps) {
        this.yearlyQuotaMaps = yearlyQuotaMaps;
    }

    public Map<FishState, MultiQuotaRegulation> getModelQuota() {
        return modelQuota;
    }
}
