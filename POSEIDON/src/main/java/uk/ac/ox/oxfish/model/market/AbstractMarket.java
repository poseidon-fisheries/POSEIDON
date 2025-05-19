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

package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.Gatherer;
import uk.ac.ox.oxfish.model.data.collectors.Counter;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.data.collectors.TimeSeries;
import uk.ac.ox.oxfish.model.regs.Regulation;

import java.util.logging.Logger;

import static tech.units.indriya.unit.Units.KILOGRAM;

/**
 * Adds data collection to the interface
 * Created by carrknight on 5/3/15.
 */
public abstract class AbstractMarket implements MarketWithCounter {


    public static final String LANDINGS_COLUMN_NAME = "Landings";
    public static final String EARNINGS_COLUMN_NAME = "Earnings";
    public static final String PRICE_COLUMN_NAME = "Marginal Price";


    private final Counter dailyCounter;


    private final TimeSeries<Market> dailyObservations = new TimeSeries<>(IntervalPolicy.EVERY_DAY);

    /**
     * flag to avoid starting multiple times if start is called repeatedly
     */
    protected boolean started = false;

    /**
     * species we are trading
     */
    private Species species;

    public AbstractMarket() {
        dailyCounter = new Counter(IntervalPolicy.EVERY_DAY);
    }

    public Species getSpecies() {
        return species;
    }

    public void setSpecies(final Species species) {
        this.species = species;
    }

    /**
     * starts gathering data. If called multiple times all the calls after the first are ignored
     *
     * @param state the model
     */
    @Override
    public void start(final FishState state) {
        Preconditions.checkArgument(species != null, " market doesn't know the species to trade in");
        if (started) //don't start twice
            return;

        //start the counter
        dailyCounter.start(state);
        dailyCounter.addColumn(EARNINGS_COLUMN_NAME);
        dailyCounter.addColumn(LANDINGS_COLUMN_NAME);
        dailyCounter.addColumn(PRICE_COLUMN_NAME);

        //start the data-set where we are going to store the history of the counter
        dailyObservations.start(state, this);
        //the gatherers reset the counters as a side effect
        dailyObservations.registerGatherer(EARNINGS_COLUMN_NAME,
            (Gatherer<Market>) market -> dailyCounter.getColumn(EARNINGS_COLUMN_NAME),
            Double.NaN, dailyObservations.getCurrency(), "Earnings"
        );

        dailyObservations.registerGatherer(LANDINGS_COLUMN_NAME,
            (Gatherer<Market>) market -> dailyCounter.getColumn(LANDINGS_COLUMN_NAME),
            Double.NaN, KILOGRAM, "Biomass"
        );

        dailyObservations.registerGatherer(PRICE_COLUMN_NAME, Market::getMarginalPrice,
            Double.NaN, dailyObservations.getCurrency(), "Price"
        );

        started = true;

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        dailyCounter.turnOff();
        dailyObservations.turnOff();
    }

    /**
     * Sells the a specific amount of fish here by calling sellFishImplementation and then store the trade result details
     *
     * @param hold
     * @param fisher     the seller
     * @param regulation the regulation object the seller abides to
     * @param state      the model
     */
    @Override
    final public TradeInfo sellFish(
        final Hold hold, final Fisher fisher, final Regulation regulation,
        final FishState state, final Species species
    ) {
        Preconditions.checkArgument(species == this.species, "trading the wrong species!");
        final TradeInfo receipt = sellFishImplementation(
            hold,
            fisher, regulation, state, species
        );
        recordTrade(receipt);
        return receipt;
    }

    protected abstract TradeInfo sellFishImplementation(
        Hold hold, Fisher fisher, Regulation regulation, FishState state, Species species
    );


    public void recordTrade(final TradeInfo info) {
        if (info.getBiomassTraded() > 0)
            Logger.getGlobal().fine(() -> "recorded the following trade: " + info);
        dailyCounter.count(EARNINGS_COLUMN_NAME, info.getMoneyExchanged());
        dailyCounter.count(LANDINGS_COLUMN_NAME, info.getBiomassTraded());


    }


    public TimeSeries<Market> getData() {
        return dailyObservations;
    }

    public Counter getDailyCounter() {
        return dailyCounter;
    }

    @Override
    public boolean isStarted() {
        return started;
    }
}
