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
import uk.ac.ox.oxfish.model.regs.Regulation;

/**
 * A market where price drops if too much biomass accumulates
 * Created by carrknight on 8/11/15.
 */
public class CongestedMarket extends AbstractBiomassMarket implements Steppable{


    /**
     * if in the market there is less than this biomass available, there is no penalty to price
     */
    private double  acceptableBiomassThreshold;


    /**
     * maximum price of the market
     */
    private double maxPrice;

    /**
     * by how much does price decrease in proportion to how much we are above biomass threshold. It's basically $/weight
     */
    private double demandSlope;

    /**
     * how much biomass for this fish gets consumed each day (and removed from the market)
     */
    private double dailyConsumption;

    /**
     * if 1 the market consumes stock every day, otherwise the market consumes stock every x days (but multiplies the dailyConsumption accordingly)
     */
    private final int consumptionPeriod;


    /**
     * how much biomass is here, waiting to be consumed
     */
    private double biomassHere = 0;
    private Stoppable stoppable;


    public CongestedMarket(
            double acceptableBiomassThreshold, double maxPrice, double discountRate,
            double dailyConsumption) {
        this(acceptableBiomassThreshold, maxPrice, discountRate, dailyConsumption,1);
    }


    public CongestedMarket(
            double acceptableBiomassThreshold, double maxPrice, double discountRate,
            double dailyConsumption, int consumptionPeriod) {
        super();
        this.acceptableBiomassThreshold = acceptableBiomassThreshold;
        this.maxPrice = maxPrice;
        this.demandSlope = discountRate;
        this.dailyConsumption = dailyConsumption;
        Preconditions.checkArgument(consumptionPeriod>0);
        this.consumptionPeriod = consumptionPeriod;
    }

    /**
     * starts gathering data. If called multiple times all the calls after the first are ignored
     *
     * @param state the model
     */
    @Override
    public void start(FishState state) {
        super.start(state);

        if(consumptionPeriod==1)
            stoppable = state.scheduleEveryDay(this, StepOrder.DAWN);
        else
            stoppable = state.scheduleEveryXDay(this,StepOrder.DAWN,consumptionPeriod);
    }


    @Override
    public void step(SimState simState) {
        System.out.println(getMarginalPrice());
        biomassHere = Math.max(0, biomassHere - dailyConsumption * consumptionPeriod);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        super.turnOff();
        if(stoppable!= null)
            stoppable.stop();
    }

    /**
     * the only method to implement for subclasses. Needs to actually do the trading and return the result
     *
     * @param biomass    the biomass caught by the seller
     * @param fisher     the seller
     * @param regulation the rules the seller abides to
     * @param state      the model
     * @return TradeInfo  results
     */
    @Override
    protected TradeInfo sellFishImplementation(
            double biomass, Fisher fisher, Regulation regulation, FishState state,
            Species species) {

        //find out legal biomass sold
        double biomassActuallySellable = Math.min(biomass,
                                                  regulation.maximumBiomassSellable(fisher, species, state));
        if(biomassActuallySellable <=0)
            return new TradeInfo(0, species, 0);


        biomassHere+=biomassActuallySellable;
        double revenue = biomassActuallySellable * computePrice(biomassHere);

        assert revenue >=0;
        //give fisher the money
        fisher.earn(revenue);

        //tell regulation
        regulation.reactToSale(species, fisher , biomassActuallySellable, revenue, state);

        //return biomass sellable
        return new TradeInfo(biomassActuallySellable, species, revenue);



    }


    private double getOvershoot(double totalBiomass)
    {
        return Math.max(0,totalBiomass-acceptableBiomassThreshold);
    }

    private double computePrice(double totalBiomassHere){
        return Math.max(0, maxPrice - getOvershoot(totalBiomassHere) * demandSlope);
    }

    public double getMarginalPrice()
    {
        return computePrice(biomassHere);
    }

    public double getAcceptableBiomassThreshold() {
        return acceptableBiomassThreshold;
    }

    public double getBiomassHere() {
        return biomassHere;
    }
}
