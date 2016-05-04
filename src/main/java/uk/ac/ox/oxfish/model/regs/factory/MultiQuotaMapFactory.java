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

    private StringBuilder representer;

    private int quotaExchangedPerMatch = 100;

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
    private void representMapAsString(List<Species> species){

        //we will use the string builder to build the string representation of the quota file
        representer = new StringBuilder();

        //check that there are some fish being protected or what's the point?
        Preconditions.checkState(!initialQuotas.isEmpty());
        for(Map.Entry<String,Double> quota : initialQuotas.entrySet())
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

        itqFactory.setYearlyQuotaMaps(representer.toString());
        tacFactory.setYearlyQuotaMaps(representer.toString());





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
        if(representer == null)
                representMapAsString(fishState.getSpecies()
                );

        assert representer!=null;


        if(itq) {
            itqFactory.setAllowMultipleTrades(multipleTradesAllowed);
            itqFactory.setMinimumQuotaTraded(quotaExchangedPerMatch);
            MultiQuotaRegulation regulation = itqFactory.apply(fishState);
            //set up a startable that divide it by the number of fishers
            fishState.registerStartable(new ITQScaler(regulation));
            return regulation;
        }
        else
            return tacFactory.apply(fishState);




    }

    public StringBuilder getRepresenter() {
        return representer;
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

    public int getQuotaExchangedPerMatch() {
        return quotaExchangedPerMatch;
    }

    public void setQuotaExchangedPerMatch(int quotaExchangedPerMatch) {
        this.quotaExchangedPerMatch = quotaExchangedPerMatch;
    }

    public HashMap<String, Double> getInitialQuotas() {
        return initialQuotas;
    }

    public void setInitialQuotas(HashMap<String, Double> quotas) {
        this.initialQuotas = quotas;
    }
}
