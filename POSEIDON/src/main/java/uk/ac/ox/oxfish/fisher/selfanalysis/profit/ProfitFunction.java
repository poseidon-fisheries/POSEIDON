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

package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;

/**
 * A container to judge the profits of a trip after the trip is over or to simulate a trip and guess profits that way
 * Created by carrknight on 7/13/16.
 */
public class ProfitFunction {


    /**
     * needed to predict profits from trip
     */
    private final LameTripSimulator simulator;
    private final double maxHours;
    /**
     * separate from the other because they are always there
     */
    private final Cost oilCosts = new GasCost();


    public ProfitFunction(
        final double maxHours
    ) {

        this(new LameTripSimulator(), maxHours);
    }

    public ProfitFunction(
        final LameTripSimulator simulator, final double maxHours
    ) {
        this.simulator = simulator;
        this.maxHours = maxHours;
    }

    public double hourlyProfitFromHypotheticalTripHere(
        final Fisher fisher, final SeaTile where, final FishState state,
        final double[] catchExpectations, final boolean verbose
    ) {
        return simulateHourlyProfits(
            fisher,
            catchExpectations,
            where,
            state,
            verbose
        );


    }

    /**
     * compute hourly profits for this trip given current information (so if prices of fish changed since this trip is over
     * the profits computed here and the profits recorded in the trip won't be the same)
     *
     * @param fisher agent who made the trip or anyway whose point of view matters
     * @param state  the model
     * @return $/hr profits of this trip
     */
    public Double simulateHourlyProfits(
        final Fisher fisher, final double[] expectedCatches,
        final SeaTile where,
        final FishState state, final boolean verbose
    ) {

        final TripRecord trip = simulateTrip(fisher, expectedCatches, where, state);
        if (trip == null)
            return Double.NaN;
        if (expectedCatches == null)
            return Double.NaN;


        if (verbose) {
            final double expectedTotalCatchesPerHour = trip.getEffort() == 0 ? 0 :
                Arrays.stream(trip.getTotalCatch()).sum() / trip.getEffort();
            final double hoursNeeded = expectedTotalCatchesPerHour == 0 ? Double.POSITIVE_INFINITY : fisher.getMaximumHold() / expectedTotalCatchesPerHour;
            System.out.println("Going to " + trip.getMostFishedTileInTrip() + " I will spend "
                + (trip.getDurationInHours() - trip.getEffort()) + " travelling plus " +
                trip.getEffort() + " fishing, expecting " + expectedTotalCatchesPerHour + " lbs of " +
                "catch per hour which implies " +
                hoursNeeded + " hours to fill the boat; in total I am going to travel "
                + trip.getDistanceTravelled() + " km and consume " + trip.getLitersOfGasConsumed() + " liters of gas");

            System.out.println("I predict earnings of " + trip.getEarnings()
                + " with costs " + trip.getTotalCosts()
                + " of which opportunity costs are : " + trip.getOpportunityCosts());
            System.out.println("I predict profits of " + trip.getTotalTripProfit() + " which means per hour of  " +
                trip.getProfitPerHour(true));
        }


        return trip.getProfitPerHour(true);
    }

    public TripRecord simulateTrip(
        final Fisher fisher,
        final double[] expectedCatches,
        final SeaTile where,
        final FishState state
    ) {

        for (int i = 0; i < expectedCatches.length; i++)
            if (!Double.isFinite(expectedCatches[i]))
                return null;

        final TripRecord trip = LameTripSimulator.simulateRecord(
            fisher,
            where,
            state,
            maxHours,
            expectedCatches
        );
        if (trip == null)
            return null;

        recordCostsToTrip(fisher, trip, state);
        return trip;
    }

    private void recordCostsToTrip(final Fisher fisher, final TripRecord trip, final FishState state) {
        final double[] catches = trip.getSoldCatch();
        double earnings = 0;
        for (final Species species : state.getSpecies())
            earnings += catches[species.getIndex()] * fisher.getHomePort().getMarginalPrice(species, fisher);


        double costs = oilCosts.cost(fisher, state, trip, earnings, trip.getDurationInHours());
        for (final Cost otherCost : fisher.getAdditionalTripCosts())
            costs += otherCost.cost(fisher, state, trip, earnings, trip.getDurationInHours());
        trip.recordCosts(costs);

        costs = 0;
        for (final Cost opportunity : fisher.getOpportunityCosts())
            costs += opportunity.cost(fisher, state, trip, earnings, trip.getDurationInHours());
        trip.recordOpportunityCosts(costs);

    }


}
