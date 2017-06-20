package uk.ac.ox.oxfish.model.regs.factory;

import com.esotericsoftware.minlog.Log;
import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Creates either a multi-itq or a multi-tac reading from file
 * Created by carrknight on 4/5/16.
 */
public class MultiQuotaMapFactory implements AlgorithmFactory<MultiQuotaRegulation>{


    private boolean itq=true;

    private HashMap<String,Double> initialQuotas = new HashMap<>();

    private String convertedInitialQuotas;

    private String convertedQuotaExchangedPerMatch;

    /**
     * the size of quota units (kg) traded each match;
     * This can be either a simple number (at which point all quotas are traded at the same tick volume) or a map like
     * "0:100,2:50" at which point the volume traded per match is different for each species
     */

    private HashMap<String,Double> quotaExchangedPerMatch = new HashMap<>();


    private boolean multipleTradesAllowed = false;

    //even though we only one use delegate we keep both available
    private MultiITQStringFactory itqFactory = new MultiITQStringFactory();

    private MultiTACStringFactory tacFactory  = new MultiTACStringFactory();


    public MultiQuotaMapFactory() {
    }

    public MultiQuotaMapFactory(boolean itq, Pair<String,Double>... pairs) {

        this.itq = itq;
        for(Pair<String,Double> pair : pairs)
            initialQuotas.put(pair.getFirst(),pair.getSecond());
    }

    /**
     * we parse the file into a string that can be fed to delegate factories
     */
    public static String representMapAsString(List<Species> species, final HashMap<String, Double> toConvert){

        //we will use the string builder to build the string representation of the quota file
        StringBuilder representer = new StringBuilder();

        //check that there are some fish being protected or what's the point?
        Preconditions.checkState(!toConvert.isEmpty());
        for(Map.Entry<String,Double> quota : toConvert.entrySet())
        {


            //find which species it belongs to
            Optional<Species> matchingSpecies = species.stream().filter(species1 ->
                                                                                species1.getName().equalsIgnoreCase(
                                                                                        quota.getKey().trim())).findAny();
            //we didn't find a corresponding species, that's possibly a problem so warn
            if(!matchingSpecies.isPresent() && Log.WARN)
                Log.warn("Could not find " + quota.getKey() + " in the list of model species, I will ignore its quota");
            else
            {
                int index = matchingSpecies.get().getIndex();
                if(representer.length()>0)
                    representer.append(",");
                representer.append(index).append(":").append(quota.getValue());
            }

        }
        return representer.toString();






    }



    public boolean isItq() {
        return itq;
    }

    public void setItq(boolean itq) {
        this.itq = itq;
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(FishState fishState) {
        //turn maps into strings so that they can be fed to the factories
        if(convertedInitialQuotas == null) {
            convertedInitialQuotas =representMapAsString(fishState.getSpecies(), initialQuotas);

            itqFactory.setYearlyQuotaMaps(convertedInitialQuotas);
            tacFactory.setYearlyQuotaMaps(convertedInitialQuotas);
        }
        assert convertedInitialQuotas!=null;



        if(itq) {
            if(convertedQuotaExchangedPerMatch == null)
            {
                convertedQuotaExchangedPerMatch = representMapAsString(fishState.getSpecies(),quotaExchangedPerMatch);
                itqFactory.setMinimumQuotaTraded(convertedQuotaExchangedPerMatch);

            }
            assert convertedQuotaExchangedPerMatch!=null;

            itqFactory.setAllowMultipleTrades(multipleTradesAllowed);
            MultiQuotaRegulation regulation = itqFactory.apply(fishState);
            //set up a startable that divide it by the number of fishers
            fishState.registerStartable(new ITQScaler(regulation));
            return regulation;
        }
        else
            return tacFactory.apply(fishState);




    }


    static class ITQScaler implements Startable
    {

        private final MultiQuotaRegulation toScale;

        public ITQScaler(MultiQuotaRegulation toScale) {
            this.toScale = toScale;
        }

        @Override
        public void start(FishState model) {
            for (int i = 0; i < model.getSpecies().size(); i++) {
                double availableQuota = toScale.getQuotaRemaining(i);
                if (Double.isFinite(availableQuota))
                    toScale.setYearlyQuota(i,
                                              availableQuota /
                                                      model.getNumberOfFishers());
            }

        }

        @Override
        public void turnOff() {

        }

    }

    public boolean isMultipleTradesAllowed() {
        return multipleTradesAllowed;
    }

    public void setMultipleTradesAllowed(boolean multipleTradesAllowed) {
        this.multipleTradesAllowed = multipleTradesAllowed;
    }


    /**
     * Getter for property 'quotaExchangedPerMatch'.
     *
     * @return Value for property 'quotaExchangedPerMatch'.
     */
    public HashMap<String, Double> getQuotaExchangedPerMatch() {
        return quotaExchangedPerMatch;
    }

    /**
     * Setter for property 'quotaExchangedPerMatch'.
     *
     * @param quotaExchangedPerMatch Value to set for property 'quotaExchangedPerMatch'.
     */
    public void setQuotaExchangedPerMatch(HashMap<String, Double> quotaExchangedPerMatch) {
        this.quotaExchangedPerMatch = quotaExchangedPerMatch;
    }

    public HashMap<String, Double> getInitialQuotas() {
        return initialQuotas;
    }

    public void setInitialQuotas(HashMap<String, Double> quotas) {
        this.initialQuotas = quotas;
    }

    /**
     * Getter for property 'convertedInitialQuotas'.
     *
     * @return Value for property 'convertedInitialQuotas'.
     */
    public String getConvertedInitialQuotas() {
        return convertedInitialQuotas;
    }

    /**
     * Getter for property 'convertedQuotaExchangedPerMatch'.
     *
     * @return Value for property 'convertedQuotaExchangedPerMatch'.
     */
    public String getConvertedQuotaExchangedPerMatch() {
        return convertedQuotaExchangedPerMatch;
    }
}
