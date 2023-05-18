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

package uk.ac.ox.oxfish.model.market;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * A market where there is a linear demand p = a - b *q where q is the lagged MA (moving average) of previous days sales
 * Created by carrknight on 1/6/16.
 */
public class MACongestedMarket extends AbstractBiomassMarket implements Steppable {

    /**
     * the moving average holding the previous days sales and whose smoothed value represents the price congestion
     */
    private final MovingAverage<Double> congestion;
    /**
     * price when congestion is 0, basically the intercept of the demand curve
     */
    private final double priceWithNoCongestion;
    /**
     * the demand slope, that is how much the price drops in $ for every unit of lagged and smoothed biomass sold
     */
    private final double demandSlope;
    /**
     * today sales, doesn't affect today's price
     */
    private double todayCongestion = 0;
    /**
     * today's price, computed in the step()
     */
    private double todayPrice;
    /**
     * object to stop this market from stepping
     */
    private Stoppable stoppable;


    /**
     * The congested market with lagged and smoothed congestion
     *
     * @param priceWithNoCongestion price with no congestion
     * @param demandSlope           the slope ($/biomass) of the demand curve defining how much
     * @param observationWindow     the size of the moving average holding previous days sales as "congestion"
     */
    public MACongestedMarket(double priceWithNoCongestion, double demandSlope, int observationWindow) {
        this.priceWithNoCongestion = priceWithNoCongestion;
        todayPrice = priceWithNoCongestion;
        Preconditions.checkArgument(
            demandSlope >= 0,
            "Demand slope supplied cannot be negative (I'll flip the sign later)"
        );
        this.demandSlope = demandSlope;
        congestion = new MovingAverage<>(observationWindow);
    }


    /**
     * starts gathering data. If called multiple times all the calls after the first are ignored
     *
     * @param state the model
     */
    @Override
    public void start(FishState state) {
        super.start(state);
        stoppable = state.scheduleEveryDay(this, StepOrder.POLICY_UPDATE);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        super.turnOff();
        if (stoppable != null)
            stoppable.stop();
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     *
     * @param biomass    the biomass caught by the seller
     * @param fisher     the seller
     * @param regulation the rules the seller abides to
     * @param state      the model
     * @param species    the species being traded
     * @return TradeInfo  results
     */
    @Override
    protected TradeInfo sellFishImplementation(
        double biomass, Fisher fisher, Regulation regulation, FishState state, Species species
    ) {
        //find out legal biomass sold
        double biomassActuallySellable = Math.min(
            biomass,
            regulation.maximumBiomassSellable(fisher, species, state)
        );
        if (biomassActuallySellable <= 0)
            return new TradeInfo(0, species, 0);

        assert biomassActuallySellable > 0;
        todayCongestion += biomassActuallySellable;
        assert todayCongestion > 0;
        double revenue = biomassActuallySellable * todayPrice;


        assert revenue >= 0;
        //give fisher the money
        fisher.earn(revenue);

        //tell regulation
        regulation.reactToSale(species, fisher, biomassActuallySellable, revenue, state);

        return new TradeInfo(biomassActuallySellable, species, revenue);
    }

    /**
     * how much do you intend to pay the next epsilon amount of biomass sold here
     *
     * @return price
     */
    @Override
    public double getMarginalPrice() {
        return todayPrice;
    }

    @Override
    public void step(SimState simState) {
        congestion.addObservation(todayCongestion);
        todayCongestion = 0;
        todayPrice = Math.max(0, priceWithNoCongestion - demandSlope * congestion.getSmoothedObservation());
    }
}
