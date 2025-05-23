/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.KitchenSinkRegulation;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.TemporaryProtectedArea;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;

import java.util.List;

/**
 * A factory for the kitchen sink regulation, it is itself just a collection of factories
 * Created by carrknight on 12/9/15.
 */
public class KitchenSinkFactory implements AlgorithmFactory<KitchenSinkRegulation> {

    final private TemporaryProtectedAreasFactory mpa = new TemporaryProtectedAreasFactory();
    final private FishingSeasonFactory seasons = new FishingSeasonFactory();
    final private MultiITQStringFactory itqFactory = new MultiITQStringFactory();
    final private MultiTACStringFactory tacFactory = new MultiTACStringFactory();
    boolean individualTradeableQuotas = true;

    public KitchenSinkFactory() {
        //we are going to sync them
        tacFactory.setYearlyQuotaMaps(itqFactory.getYearlyQuotaMaps());

    }


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public KitchenSinkRegulation apply(final FishState fishState) {
        final TemporaryProtectedArea subcomponent1 = mpa.apply(fishState);
        final FishingSeason subcomponent2 = seasons.apply(fishState);

        final MultiQuotaRegulation subcomponent3;
        if (individualTradeableQuotas)
            subcomponent3 = itqFactory.apply(fishState);
        else
            subcomponent3 = tacFactory.apply(fishState);

        final KitchenSinkRegulation reg = new KitchenSinkRegulation(
            subcomponent1,
            subcomponent2,
            subcomponent3
        );

        if (individualTradeableQuotas) {
            //initializes
            itqFactory.apply(fishState);
            for (final ITQMarketBuilder builder : itqFactory.getOrderBooksBuilder().presentKey(
                fishState.getUniqueID(),
                () -> {
                    throw new RuntimeException("Should be initialized already!!!");
                }
            ))
                if (builder != null)
                    builder.addTrader(reg);
        }
        return reg;

    }


    public boolean isIndividualTradeableQuotas() {
        return individualTradeableQuotas;
    }

    public void setIndividualTradeableQuotas(final boolean individualTradeableQuotas) {
        this.individualTradeableQuotas = individualTradeableQuotas;
    }

    public DoubleParameter getSeasonLength() {
        return seasons.getSeasonLength();
    }

    public void setSeasonLength(final DoubleParameter seasonLength) {
        seasons.setSeasonLength(seasonLength);
    }

    public String getYearlyQuotaMaps() {
        return itqFactory.getYearlyQuotaMaps();
    }

    public void setYearlyQuotaMaps(final String yearlyQuotaMaps) {
        itqFactory.setYearlyQuotaMaps(yearlyQuotaMaps);
        tacFactory.setYearlyQuotaMaps(yearlyQuotaMaps);
    }

    /**
     * Getter for property 'startDay'.
     *
     * @return Value for property 'startDay'.
     */
    public DoubleParameter getStartDay() {
        return mpa.getStartDay();
    }

    /**
     * Setter for property 'startDay'.
     *
     * @param startDay Value to set for property 'startDay'.
     */
    public void setStartDay(final DoubleParameter startDay) {
        mpa.setStartDay(startDay);
    }

    /**
     * Getter for property 'startingMPAs'.
     *
     * @return Value for property 'startingMPAs'.
     */
    public List<StartingMPA> getStartingMPAs() {
        return mpa.getStartingMPAs();
    }

    /**
     * Setter for property 'startingMPAs'.
     *
     * @param startingMPAs Value to set for property 'startingMPAs'.
     */
    public void setStartingMPAs(final List<StartingMPA> startingMPAs) {
        mpa.setStartingMPAs(startingMPAs);
    }


    /**
     * Getter for property 'duration'.
     *
     * @return Value for property 'duration'.
     */
    public DoubleParameter getDuration() {
        return mpa.getDuration();
    }

    /**
     * Setter for property 'duration'.
     *
     * @param duration Value to set for property 'duration'.
     */
    public void setDuration(final DoubleParameter duration) {
        mpa.setDuration(duration);
    }
}
