package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.yaml.YamlConstructor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A more flexible, if slightly convoluted, way to instantiate an ITQ targeting only a few species
 * Created by carrknight on 11/9/15.
 */
public class MultiITQStringFactory implements AlgorithmFactory<MultiQuotaRegulation>{

    /**
     * an array of order books for each "model" run
     */
    private final Map<FishState,ITQOrderBook[]> orderBooks = new HashMap<>(1);

    /**
     * an array of order book makers for each model run
     */
    private final Map<FishState,ITQMarketBuilder[]> orderBooksBuilder = new HashMap<>(1);

    /**
     * The string we are going to turn into rule, "0:100 ,2:uniform 1 100" means that EACH FISHER gets 100 quotas a year
     * for species 0 and a random quota of 1 to 100 for species 2. The other species (like species 1)
     * are then assumed NOT TO BE PROTECTED by the quota (and can be fished out freely)
     */
    private String yearlyQuotaMaps = "0:5000";


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState state)
    {
        Map<String, String> quotasInputted = Splitter.on(",").withKeyValueSeparator(":").split(yearlyQuotaMaps.trim());
        Preconditions.checkArgument(quotasInputted.size() > 0, "You provided no quota for the ITQ!");
        //here we store the quotas
        int numberOfSpecies = state.getSpecies().size();
        double[] quotas = new double[numberOfSpecies];
        Arrays.fill(quotas,Double.POSITIVE_INFINITY);
        //read them in
        for(Map.Entry<String,String> input : quotasInputted.entrySet())
        {
            Double yearlyQuota = YamlConstructor.parseDoubleParameter(input.getValue().trim()).apply(state.getRandom());
            Preconditions.checkArgument(yearlyQuota>0);
            Preconditions.checkArgument(!yearlyQuota.isNaN());
            quotas[Integer.parseInt(input.getKey().trim())] = yearlyQuota;
        }

        /***
         *      __  __   _   ___ _  _____ _____   ___ _   _ ___ _    ___  ___ ___  ___
         *     |  \/  | /_\ | _ \ |/ / __|_   _| | _ ) | | |_ _| |  |   \| __| _ \/ __|
         *     | |\/| |/ _ \|   / ' <| _|  | |   | _ \ |_| || || |__| |) | _||   /\__ \
         *     |_|  |_/_/ \_\_|_\_|\_\___| |_|   |___/\___/|___|____|___/|___|_|_\|___/
         *
         */

        MultiITQFactory.buildITQMarketsIfNeeded(state, numberOfSpecies, quotas, orderBooks, orderBooksBuilder);


        return MultiITQFactory.opportunityCostAwareQuotaRegulation(state,quotas,orderBooks.get(state));




    }

    public String getYearlyQuotaMaps() {
        return yearlyQuotaMaps;
    }

    public void setYearlyQuotaMaps(String yearlyQuotaMaps) {
        this.yearlyQuotaMaps = yearlyQuotaMaps;
    }
}
