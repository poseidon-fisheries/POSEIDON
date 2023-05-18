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
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

public class FisherEntryConstantRate implements EntryPlugin {


    /**
     * which population of boats are we growing; this has to be both the name of the fishery factory and a tag so that we know
     * which boats belong to it
     */
    private final String populationName;
    private final int doNotGrowBeforeThisYear;
    /**
     * we expect activeBoats * (growthRateInPercentage) to be the new entrants next year
     */
    private double growthRateInPercentage;
    private Stoppable stoppable;
    private boolean isEntryPaused = false;


    public FisherEntryConstantRate(double growthRateInPercentage, String populationName) {
        this(growthRateInPercentage, populationName, -1);
    }


    public FisherEntryConstantRate(double growthRateInPercentage, String populationName, int doNotGrowBeforeThisYear) {
        this.growthRateInPercentage = growthRateInPercentage;
        this.populationName = populationName;
        this.doNotGrowBeforeThisYear = doNotGrowBeforeThisYear;
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Preconditions.checkArgument(stoppable == null, "already started!");
        stoppable = model.scheduleEveryYear(
            this,
            StepOrder.AFTER_DATA
        );


        if (!model.getEntryPlugins().contains(this))
            model.getEntryPlugins().add(this);
    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

        if (stoppable != null)
            stoppable.stop();

    }

    @Override
    public void step(SimState simState) {

        FishState model = ((FishState) simState);
        if (isEntryPaused || model.getYear() < doNotGrowBeforeThisYear)
            return;


        double currentActiveFishers = 0;
        // count the fisher as active if it has been on at least a trip in the past 365 days!
        for (Fisher fisher : model.getFishers()) {
            if (fisher.getTags().contains(populationName) && fisher.hasBeenActiveThisYear())
                currentActiveFishers++;


        }

        long newEntrants = Math.round(currentActiveFishers * growthRateInPercentage);
        for (int i = 0; i < newEntrants; i++)
            model.createFisher(populationName);

    }

    /**
     * Getter for property 'growthRateInPercentage'.
     *
     * @return Value for property 'growthRateInPercentage'.
     */
    public double getGrowthRateInPercentage() {
        return growthRateInPercentage;
    }

    /**
     * Setter for property 'growthRateInPercentage'.
     *
     * @param growthRateInPercentage Value to set for property 'growthRateInPercentage'.
     */
    public void setGrowthRateInPercentage(double growthRateInPercentage) {
        this.growthRateInPercentage = growthRateInPercentage;
    }

    /**
     * Getter for property 'populationName'.
     *
     * @return Value for property 'populationName'.
     */
    public String getPopulationName() {
        return populationName;
    }

    @Override
    public boolean isEntryPaused() {
        return isEntryPaused;
    }

    @Override
    public void setEntryPaused(boolean entryPaused) {
        isEntryPaused = entryPaused;
    }
}
