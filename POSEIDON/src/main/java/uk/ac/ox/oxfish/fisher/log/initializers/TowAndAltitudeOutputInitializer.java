/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.fisher.log.initializers;

import sim.engine.SimState;
import sim.engine.Steppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.AltitudeOutput;
import uk.ac.ox.oxfish.model.data.DiscretizationHistogrammer;
import uk.ac.ox.oxfish.model.data.TowOutput;

/**
 * generates exclusively the histogram initializer
 */
public class TowAndAltitudeOutputInitializer implements AdditionalStartable, LogbookInitializer {



    private final int histogrammerStartYear;


    /**
     * useful to discriminate between multiple outputs
     */
    private final String identifier;

    private TowOutput tows;




    public TowAndAltitudeOutputInitializer(int histogrammerStartYear, String identifier) {
        this.histogrammerStartYear = histogrammerStartYear;
        this.identifier = identifier;
    }



    @Override
    public void add(Fisher fisher, FishState state) {


        //add histogrammer now or when it is time!
        if(histogrammerStartYear>=0) { //don't do anything if the start year is negative!
            if (state.getYear() >= histogrammerStartYear)
                fisher.addTripListener(tows);
            else
                state.scheduleOnceAtTheBeginningOfYear(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        fisher.addTripListener(tows);
                    }
                }, StepOrder.DAWN, histogrammerStartYear);
        }



    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(FishState model) {

        //let it build, we won't start it until it's time though
        tows = new TowOutput(model.getMap());
        tows.setFileName(identifier +"_" + tows.getFileName());
        model.getOutputPlugins().add(tows);


        AltitudeOutput altitude = new AltitudeOutput(model.getMap());
        altitude.setFileName( identifier + altitude.getFileName());
        model.getOutputPlugins().add(altitude);


    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {

    }
}
