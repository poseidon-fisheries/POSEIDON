/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.model.regs.factory;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Creates either a multi-itq or a multi-tac reading from file
 * Created by carrknight on 4/5/16.
 */
public class MultiQuotaMapFactory implements AlgorithmFactory<MultiQuotaRegulation> {


    //even though we only one use delegate we keep both available
    private final MultiITQStringFactory itqFactory = new MultiITQStringFactory();
    private final MultiTACStringFactory tacFactory = new MultiTACStringFactory();
    private final MultiIQStringFactory iqFactory = new MultiIQStringFactory();
    private HashMap<String, Double> initialQuotas = new HashMap<>();
    private String convertedInitialQuotas;
    private String convertedQuotaExchangedPerMatch;
    private boolean respectMPA = true;
    private QuotaType quotaType = QuotaType.ITQ;
    /**
     * the size of quota units (kg) traded each match;
     * This can be either a simple number (at which point all quotas are traded at the same tick volume) or a map like
     * "0:100,2:50" at which point the volume traded per match is different for each species
     */

    private HashMap<String, Double> quotaExchangedPerMatch = new HashMap<>();
    private boolean multipleTradesAllowed = false;

    public MultiQuotaMapFactory() {
    }


    @SuppressWarnings("unchecked")
    public MultiQuotaMapFactory(final QuotaType quotaType, final Map.Entry<String, Double>... pairs) {

        this.quotaType = quotaType;
        for (final Map.Entry<String, Double> pair : pairs)
            initialQuotas.put(pair.getKey(), pair.getValue());
    }

    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public MultiQuotaRegulation apply(final FishState fishState) {
        //turn maps into strings so that they can be fed to the factories
        if (convertedInitialQuotas == null) {
            convertedInitialQuotas = representMapAsString(fishState.getSpecies(), initialQuotas);

            itqFactory.setYearlyQuotaMaps(convertedInitialQuotas);
            tacFactory.setYearlyQuotaMaps(convertedInitialQuotas);
            iqFactory.setYearlyQuotaMaps(convertedInitialQuotas);
        }
        assert convertedInitialQuotas != null;


        switch (quotaType) {
            case ITQ:
                if (convertedQuotaExchangedPerMatch == null) {
                    convertedQuotaExchangedPerMatch = representMapAsString(
                        fishState.getSpecies(),
                        quotaExchangedPerMatch
                    );
                    itqFactory.setMinimumQuotaTraded(convertedQuotaExchangedPerMatch);

                }
                assert convertedQuotaExchangedPerMatch != null;

                itqFactory.setAllowMultipleTrades(multipleTradesAllowed);
                final MultiQuotaRegulation regulation = itqFactory.apply(fishState);
                //set up a startable that divide it by the number of fishers
                fishState.registerStartable(new ITQScaler(regulation));
                regulation.setRespectMPA(respectMPA);
                return regulation;

            case TAC:
                final MultiQuotaRegulation tac = tacFactory.apply(fishState);
                tac.setRespectMPA(respectMPA);
                return tac;
            case IQ:
                final MultiQuotaRegulation iq = iqFactory.apply(fishState); //create but scale
                fishState.registerStartable(new ITQScaler(iq));
                iq.setRespectMPA(respectMPA);
                return iq;
            default:
                throw new RuntimeException("Not a valid quota type was supplied: " + quotaType);

        }


    }

    /**
     * we parse the file into a string that can be fed to delegate factories
     */
    public static String representMapAsString(final List<Species> species, final HashMap<String, Double> toConvert) {

        //we will use the string builder to build the string representation of the quota file
        final StringBuilder representer = new StringBuilder();

        //check that there are some fish being protected or what's the point?
        Preconditions.checkState(!toConvert.isEmpty());
        for (final Map.Entry<String, Double> quota : toConvert.entrySet()) {


            //find which species it belongs to
            final Optional<Species> matchingSpecies = species.stream().filter(species1 ->
                species1.getName().equalsIgnoreCase(
                    quota.getKey().trim())).findAny();
            //we didn't find a corresponding species, that's possibly a problem so warn
            if (!matchingSpecies.isPresent())
                Logger.getGlobal().warning(() ->
                    "Could not find " + quota.getKey() + " in the list of model species, I will ignore its quota"
                );
            else {
                final int index = matchingSpecies.get().getIndex();
                if (representer.length() > 0)
                    representer.append(",");
                representer.append(index).append(":").append(quota.getValue());
            }

        }
        return representer.toString();


    }

    public boolean isMultipleTradesAllowed() {
        return multipleTradesAllowed;
    }

    public void setMultipleTradesAllowed(final boolean multipleTradesAllowed) {
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
    public void setQuotaExchangedPerMatch(final HashMap<String, Double> quotaExchangedPerMatch) {
        this.quotaExchangedPerMatch = quotaExchangedPerMatch;
    }

    public HashMap<String, Double> getInitialQuotas() {
        return initialQuotas;
    }

    public void setInitialQuotas(final HashMap<String, Double> quotas) {
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

    /**
     * Getter for property 'quotaType'.
     *
     * @return Value for property 'quotaType'.
     */
    public QuotaType getQuotaType() {
        return quotaType;
    }

    /**
     * Setter for property 'quotaType'.
     *
     * @param quotaType Value to set for property 'quotaType'.
     */
    public void setQuotaType(final QuotaType quotaType) {
        this.quotaType = quotaType;
    }

    /**
     * Getter for property 'respectMPA'.
     *
     * @return Value for property 'respectMPA'.
     */
    public boolean isRespectMPA() {
        return respectMPA;
    }

    /**
     * Setter for property 'respectMPA'.
     *
     * @param respectMPA Value to set for property 'respectMPA'.
     */
    public void setRespectMPA(final boolean respectMPA) {
        this.respectMPA = respectMPA;
    }

    public enum QuotaType {


        ITQ,

        TAC,

        IQ


    }
}
