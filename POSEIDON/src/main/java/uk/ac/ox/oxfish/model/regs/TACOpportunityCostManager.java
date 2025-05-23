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

package uk.ac.ox.oxfish.model.regs;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.engine.Stoppable;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.Startable;
import uk.ac.ox.oxfish.model.StepOrder;
import uk.ac.ox.oxfish.model.data.MovingAverage;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.fxcollections.ListChangeListener;

/**
 * A support object to count opportunity costs that are incurred in a TAC by a fisher who consumes his quotas more slowly
 * than the average
 * Created by carrknight on 10/20/15.
 */
public class TACOpportunityCostManager implements TripListener, Startable, Steppable, ListChangeListener<Fisher> {

    public static final int MOVING_AVERAGE_SIZE = 10;
    private static final long serialVersionUID = -5499519512606016930L;

    final private MovingAverage<Double>[] smoothedDailyLandings;
    final private MovingAverage<Double> smoothedHoursAtSea;

    final private MultiQuotaRegulation quotaRegulationToUse;

    private double hoursAtSeaCounter = 0d;

    private Stoppable stoppable;

    private FishState model;


    @SuppressWarnings({"unchecked", "rawtypes"})
    public TACOpportunityCostManager(final MultiQuotaRegulation quotaRegulationToUse) {
        this.quotaRegulationToUse = quotaRegulationToUse;
        smoothedDailyLandings = new MovingAverage[quotaRegulationToUse.getNumberOfSpeciesTracked()];
        smoothedHoursAtSea = new MovingAverage<>(MOVING_AVERAGE_SIZE);
    }

    /**
     * this gets called by the fish-state right after the scenario has started. It's useful to set up steppables
     * or just to percolate a reference to the model
     *
     * @param model the model
     */
    @Override
    public void start(final FishState model) {

        this.model = model;
        /*
         *    every dawn take your average!
         */
        stoppable = model.scheduleEveryDay(this, StepOrder.DAWN);


        //listen to all trips
        for (final Fisher fisher : model.getFishers())
            fisher.addTripListener(this);

        //trick to use "this" inside an anonymous function
        //also get ready to listen to new fishers
        model.getFishers().addListener(this);


        //creates the averages
        for (final Species selectedSpecies : model.getSpecies()) {
            smoothedDailyLandings[selectedSpecies.getIndex()] = new MovingAverage<>(MOVING_AVERAGE_SIZE);

        }
    }


    /**
     * if available add new data on landings to the averagers
     *
     * @param simState
     */
    @Override
    public void step(final SimState simState) {

        //if there is a fishing ban in place, no point in counting
        if (quotaRegulationToUse.isFishingStillAllowed()) {
            //count hours at sea
            smoothedHoursAtSea.addObservation(hoursAtSeaCounter);
            hoursAtSeaCounter = 0;


            //for each specie
            for (final Species selectedSpecies : model.getSpecies()) {

                //read what were yesterday's landings
                final Double observation = model.getLatestDailyObservation(selectedSpecies +
                    " " +
                    AbstractMarket.LANDINGS_COLUMN_NAME);


                //if they are a number AND if the TAC is still open (we drop censored observations)
                if (Double.isFinite(observation))
                    smoothedDailyLandings[selectedSpecies.getIndex()].addObservation(observation);

            }
        }
    }


    /**
     * computes and assigns opportunity costs to the fisher for being faster/slower than the rest
     *
     * @param record
     * @param fisher
     */
    @Override
    public void reactToFinishedTrip(final TripRecord record, final Fisher fisher) {

        hoursAtSeaCounter += record.getDurationInHours();

        for (final Species selectedSpecies : model.getSpecies()) {

            //daily catches
            final double averageDailyCatches = smoothedDailyLandings[selectedSpecies.getIndex()].getSmoothedObservation();
            final double averageHoursAtSea = smoothedHoursAtSea.getSmoothedObservation();

            if (!Double.isFinite(averageDailyCatches) || !Double.isFinite(averageHoursAtSea) || averageHoursAtSea == 0) //if we have no credible observation
                continue;

            //if on average the TAC isn't binding (more quotas available than projected to be used), then ignore
            if (averageDailyCatches * (365 - model.getDayOfTheYear()) < quotaRegulationToUse.getQuotaRemaining(
                selectedSpecies.getIndex()))
                continue;


            //make it hourly
            final double hourlyCatches = averageDailyCatches / averageHoursAtSea;


            //hourly average catches * trip length (including portside preparation)
            final double actualTripCatches = record.getSoldCatch()[selectedSpecies.getIndex()];
            assert actualTripCatches >= 0;

            final double actualHourlyCatches = actualTripCatches / record.getDurationInHours();

            final double differenceInCatchesFromAverage = hourlyCatches - actualHourlyCatches;
            //if this is positive, you are being slower than the average so in a way you are wasting quotas. If this is negative
            //then you are siphoning off quotas from competitors and that's a good thing (for you at least)
            final double price = record.getImplicitPriceReceived(selectedSpecies);

            final double opportunityCosts = price * differenceInCatchesFromAverage * record.getDurationInHours();
            record.recordOpportunityCosts(opportunityCosts);


        }


    }


    public double predictedHourlyCatches(final int speciesIndex) {
        final double averageDailyCatches = smoothedDailyLandings[speciesIndex].getSmoothedObservation();
        final double averageHoursAtSea = smoothedHoursAtSea.getSmoothedObservation();
        return averageDailyCatches / averageHoursAtSea;
    }


    /**
     * tell the startable to turnoff,
     */
    @Override
    public void turnOff() {
        stoppable.stop();
        model.getFishers().removeListener(this);
        for (final Fisher fisher : model.getFishers())
            fisher.removeTripListener(this);
    }

    /**
     * Listen to the trips of new fishers, if there are any
     */
    @Override
    public void onChanged(final Change<? extends Fisher> c) {
        for (final Fisher removed : c.getRemoved())
            removed.removeTripListener(this);
        for (final Fisher added : c.getAddedSubList())
            added.addTripListener(this);
    }
}
