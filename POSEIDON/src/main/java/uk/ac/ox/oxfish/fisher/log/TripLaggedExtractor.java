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

package uk.ac.ox.oxfish.fisher.log;

import com.google.common.base.Preconditions;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.heatmap.regression.extractors.ObservationExtractor;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.discretization.MapDiscretization;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;

import java.util.DoubleSummaryStatistics;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * self checks all trip records for the given fishers and collect its average through the year.
 */
public class TripLaggedExtractor implements Startable, Steppable, ObservationExtractor {


    private static final long serialVersionUID = -339389020566475362L;
    /**
     * this function is applied uncritically to all TripRecords to obtain a numerical value from them
     * which is then collected here
     */
    private final Function<TripRecord, Double> variableOfInterest;
    /**
     * you want to keep a separate average for each group in the discretization
     */
    private final MapDiscretization discretization;
    /**
     * the last average you computed
     */
    private final double[] lastYearAverage;
    /**
     * this is either a fisher you are following or null, in which case I expect to look at all fishers!
     */
    private Fisher fisherTracked;
    private Stoppable stoppable;


    public TripLaggedExtractor(
        final Function<TripRecord, Double> variableOfInterest,
        final MapDiscretization discretization
    ) {
        this.variableOfInterest = variableOfInterest;
        this.discretization = discretization;
        this.lastYearAverage = new double[discretization.getNumberOfGroups()];
    }

    /**
     * returns the last year average observed
     *
     * @return
     */
    @Override
    public double extract(final SeaTile tile, final double timeOfObservation, final Fisher agent, final FishState model) {
        return lastYearAverage[discretization.getGroup(tile)];
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {
        if (stoppable == null)
            stoppable = model.scheduleEveryYear(this, StepOrder.DAWN);
    }

    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        Preconditions.checkArgument(stoppable != null);
        stoppable.stop();
        stoppable = null;
    }

    @Override
    public void step(final SimState simState) {

        final FishState model = (FishState) simState;


        //if there is nobody to track you are going to sample the entire fishery!
        if (fisherTracked != null)
            update(
                fisherTracked.getFinishedTrips(),
                model.getDay()
            );
        else {
            //get all trips!
            final List<TripRecord> allTrips = new LinkedList<>();
            for (final Fisher fisher : model.getFishers()) {
                allTrips.addAll(fisher.getFinishedTrips());
            }
            update(
                allTrips,
                model.getDay()
            );

        }


    }

    //updates the averages with the new observations
    private void update(
        final List<TripRecord> totalLogs,
        final int dateToday
    ) {

        //prepare to take averages
        final DoubleSummaryStatistics[] stats = new DoubleSummaryStatistics[discretization.getNumberOfGroups()];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new DoubleSummaryStatistics();
        }

        //collect all observations
        for (final TripRecord tripRecord : totalLogs) {
            //ignore stuff that is too old
            if (tripRecord.getTripDay() >= dateToday - 365) {
                final SeaTile mostFishedTileInTrip = tripRecord.getMostFishedTileInTrip();
                if (mostFishedTileInTrip == null) //if you failed to fish anywhere, don't bother
                    continue;

                final int group = discretization.getGroup(mostFishedTileInTrip);
                final Double observation = variableOfInterest.apply(tripRecord);
                if (Double.isFinite(observation))
                    stats[group].accept(
                        observation
                    );
            }


        }
        //turn them into averages
        for (int i = 0; i < lastYearAverage.length; i++) {
            lastYearAverage[i] = stats[i].getAverage();
        }


    }

    /**
     * Getter for property 'lastYearAverage'.
     *
     * @return Value for property 'lastYearAverage'.
     */
    public double[] getLastYearAverage() {
        return lastYearAverage;
    }


    /**
     * Getter for property 'fisherTracked'.
     *
     * @return Value for property 'fisherTracked'.
     */
    public Fisher getFisherTracked() {
        return fisherTracked;
    }

    /**
     * Setter for property 'fisherTracked'.
     *
     * @param fisherTracked Value to set for property 'fisherTracked'.
     */
    public void setFisherTracked(final Fisher fisherTracked) {
        this.fisherTracked = fisherTracked;
    }
}
