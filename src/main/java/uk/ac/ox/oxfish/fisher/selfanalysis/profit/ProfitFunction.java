package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Arrays;
import java.util.function.Function;

/**
 * A container to judge the profits of a trip after the trip is over or to simulate a trip and guess profits that way
 * Created by carrknight on 7/13/16.
 */
public class ProfitFunction {


    /**
     * separate from the other because they are always there
     */
    private Cost oilCosts = new GasCost();



    /**
     * needed to predict profits from trip
     */
    private final LameTripSimulator simulator;

    private final double maxHours;



    public ProfitFunction(
            LameTripSimulator simulator, double maxHours) {
        this.simulator = simulator;
        this.maxHours = maxHours;
    }

    public ProfitFunction(
            double maxHours) {

        this(new LameTripSimulator(),maxHours);
    }

    /**
     * compute hourly profits for this trip given current information (so if prices of fish changed since this trip is over
     * the profits computed here and the profits recorded in the trip won't be the same)
     * @param fisher agent who made the trip or anyway whose point of view matters
     * @param state the model
     * @return $/hr profits of this trip
     */
    public Double simulateHourlyProfits(Fisher fisher, double[] expectedCatches,
                                        SeaTile where,
                                        FishState state, boolean verbose)
    {

        TripRecord trip = simulateTrip(fisher, expectedCatches, where, state);
        if (trip == null)
            return Double.NaN;


        if(verbose)
        {
            double expectedTotalCatchesPerHour = trip.getEffort() == 0? 0 :
                    Arrays.stream(trip.getTotalCatch()).sum() / trip.getEffort();
            double hoursNeeded = expectedTotalCatchesPerHour == 0 ? Double.POSITIVE_INFINITY : fisher.getMaximumHold()/expectedTotalCatchesPerHour;
            System.out.println("Going to " + trip.getMostFishedTileInTrip() + " I will spend "
                                       + (trip.getDurationInHours()-trip.getEffort()) + " travelling plus " +
                                       trip.getEffort() + " fishing, expecting " +expectedTotalCatchesPerHour + " lbs of " +
                                       "catch per hour which implies " +
                                       hoursNeeded +" hours to fill the boat; in total I am going to travel "
                                       + trip.getDistanceTravelled() + " km and consume " + trip.getLitersOfGasConsumed() + " liters of gas");

            System.out.println("I predict earnings of " + trip.getEarnings()
                                       + " with costs " + trip.getTotalCosts()
                                       + " of which opportunity costs are : " + trip.getOpportunityCosts());
            System.out.println("I predict profits of " + trip.getTotalTripProfit() + " which means per hour of  " +
                                       trip.getProfitPerHour(true)) ;
        }


        return  trip.getProfitPerHour(true);
    }

    public TripRecord simulateTrip(Fisher fisher, double[] expectedCatches, SeaTile where, FishState state) {
        TripRecord trip = simulator.simulateRecord(fisher,
                                                   where,
                                                   state,
                                                   maxHours, expectedCatches);
        if(trip==null)
            return null;

        recordCostsToTrip(fisher, trip, state);
        return trip;
    }

    private void recordCostsToTrip(Fisher fisher, TripRecord trip, FishState state) {
        double[] catches = trip.getSoldCatch();
        double earnings = 0;
        for(Species species : state.getSpecies())
            earnings += catches[species.getIndex()] * fisher.getHomePort().getMarginalPrice(species,fisher);


        double costs = oilCosts.cost(fisher,state,trip,earnings);
        for(Cost otherCost : fisher.getAdditionalTripCosts())
            costs+= otherCost.cost(fisher,state,trip,earnings);
        trip.recordCosts(costs);

        costs = 0;
        for(Cost opportunity : fisher.getOpportunityCosts())
            costs+= opportunity.cost(fisher,state,trip,earnings);
        trip.recordOpportunityCosts(costs);

    }


    public double hourlyProfitFromHypotheticalTripHere(
            Fisher fisher, SeaTile where, FishState state,
            Function<SeaTile, double[]> catchExpectations, boolean verbose)
    {
        return simulateHourlyProfits(fisher,
                                     catchExpectations.apply(where),
                                     where,
                                     state,
                                     verbose);




    }







}
