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
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.AltitudeHistogrammer;
import uk.ac.ox.oxfish.model.data.DiscretizationHistogrammer;

/**
 * generates exclusively the histogram initializer
 */
public class DiscretizationHistogrammerInitializer implements LogbookInitializer {


    private final MapDiscretization discretization;

    private DiscretizationHistogrammer histogrammer;

    private final int histogrammerStartYear;


    /**
     * useful to discriminate between multiple outputs
     */
    private final String identifier;


    private final boolean countEffort;


    public DiscretizationHistogrammerInitializer(
            MapDiscretization discretization, int histogrammerStartYear, String identifier, boolean countEffort) {
        this.discretization = discretization;
        this.histogrammerStartYear = histogrammerStartYear;
        this.identifier = identifier;
        this.countEffort = countEffort;


    }

    @Override
    public void add(Fisher fisher, FishState state) {


        //add histogrammer now or when it is time!
        if(histogrammerStartYear>=0) { //don't do anything if the start year is negative!
            if (state.getYear() >= histogrammerStartYear)
                fisher.addTripListener(histogrammer);
            else
                state.scheduleOnceAtTheBeginningOfYear(new Steppable() {
                    @Override
                    public void step(SimState simState) {
                        fisher.addTripListener(histogrammer);
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
        histogrammer = new DiscretizationHistogrammer(
                discretization,countEffort);
        histogrammer.setFileName( identifier + histogrammer.getFileName());
        model.getOutputPlugins().add(histogrammer);


        AltitudeHistogrammer altitude = new AltitudeHistogrammer(discretization);
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
