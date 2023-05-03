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

package uk.ac.ox.oxfish.model.event;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.*;

/**
 * updates exogenous catches every year
 */
public class BiomassDrivenTimeSeriesExogenousCatches implements ExogenousCatches{



    private final LinkedHashMap<Species, Queue<Double>> landingsTimeSeries;

    private final AbstractYearlyTargetExogenousCatches delegate;

    private Stoppable stoppable;


    public BiomassDrivenTimeSeriesExogenousCatches(
            LinkedHashMap<Species, Queue<Double>> landingsTimeSeries, final boolean allowMortalityOnFads) {

        this.landingsTimeSeries = landingsTimeSeries;

        LinkedHashMap<Species,Double> exogenousCatches = new LinkedHashMap<>();
        for(Species species : landingsTimeSeries.keySet())
            exogenousCatches.put(species,Double.NaN); //start them at NaN
        delegate = new BiomassDrivenFixedExogenousCatches(exogenousCatches, allowMortalityOnFads);

    }





    @Override
    public void step(SimState simState) {
        delegate.step(simState);
    }

    public Double getFishableBiomass(Species target, SeaTile seaTile) {
        return delegate.getFishableBiomass(target, seaTile);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {
        Preconditions.checkArgument(stoppable==null, "Already started!");
        delegate.start(model);


        updateYearlyCatches();
        stoppable = model.scheduleEveryYear((Steppable) simState1 -> updateYearlyCatches(),
                                            StepOrder.DAWN);

    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        delegate.turnOff();
        if(stoppable!=null)
            stoppable.stop();

    }


    /**
     * called every year, updates the amount of exogenous catches. Stops when it runs out of time series (which by default
     * implies the landings are fixed after that point)
     */
    public void updateYearlyCatches(){

        for (Map.Entry<Species, Queue<Double>> speciesTimeSeries : landingsTimeSeries.entrySet()) {

            Species species = speciesTimeSeries.getKey();
            Queue<Double> timeSeries = speciesTimeSeries.getValue();
            if(timeSeries.size()>0)
            {
                Double newLandings = timeSeries.poll(); //grab top and remove!
                assert newLandings !=null; //otherwise the size call is off!
                delegate.updateExogenousCatches(species,newLandings);
            }


        }


    }
}
