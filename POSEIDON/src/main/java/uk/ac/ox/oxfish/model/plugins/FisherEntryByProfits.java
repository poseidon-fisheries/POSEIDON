/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.model.plugins;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

public class FisherEntryByProfits implements EntryPlugin {


    private final String profitDataColumnName;

    private final String costsFinalColumnName;

    private final String populationName;

    private final double rateToEntryMultiplier;

    private final int maxEntrantsPerYear;


    private final double minProfitsToCoverFixedCosts;

    public boolean paused = false;

    public FisherEntryByProfits(
            String profitDataColumnName, String costsFinalColumnName, String populationName,
            double rateToEntryMultiplier, int maxEntrantsPerYear, double minProfitsToCoverFixedCosts) {
        this.profitDataColumnName = profitDataColumnName;
        this.costsFinalColumnName = costsFinalColumnName;
        this.populationName = populationName;
        this.rateToEntryMultiplier = rateToEntryMultiplier;
        this.maxEntrantsPerYear = maxEntrantsPerYear;
        this.minProfitsToCoverFixedCosts = minProfitsToCoverFixedCosts;
    }

    private Stoppable stoppable;


    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Preconditions.checkArgument(stoppable==null,"already started!");
        stoppable = model.scheduleEveryYear(this,
                                            StepOrder.POLICY_UPDATE);
        if(!model.getEntryPlugins().contains(this))
            model.getEntryPlugins().add(this);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if(stoppable!=null)
            stoppable.stop();

    }



    public int newEntrants(double averageProfits, double averageCosts)
    {

        double profitRate = (averageProfits-minProfitsToCoverFixedCosts)/(averageCosts+minProfitsToCoverFixedCosts);
        System.out.println("profit rate: " + profitRate);

        if(profitRate<=0 || !Double.isFinite(profitRate))
            return 0;
        else
            return FishStateUtilities.quickRounding(
                    profitRate*rateToEntryMultiplier);

    }


    @Override
    public void step(SimState simState) {
        if(isEntryPaused())
            return;
        FishState model = ((FishState) simState);
        int newEntrants = newEntrants(
                model.getLatestYearlyObservation(profitDataColumnName),
                model.getLatestYearlyObservation(costsFinalColumnName)
        );
        if(newEntrants>0) {
            newEntrants = Math.min(newEntrants,maxEntrantsPerYear);
            for (int i = 0; i < newEntrants; i++)
                model.createFisher(populationName);
        }
    }


    public boolean isEntryPaused() {
        return paused;
    }

    public void setEntryPaused(boolean paused) {
        this.paused = paused;
    }
}
