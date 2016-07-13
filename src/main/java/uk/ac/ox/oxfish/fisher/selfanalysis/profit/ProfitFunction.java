package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

import java.util.LinkedList;
import java.util.function.Function;

/**
 *
 * Created by carrknight on 7/13/16.
 */
public class ProfitFunction {


    /**
     * separate from the other because they are always there
     */
    private Cost oilCosts;

    /**
     * additional costs (including opportunity ones)
     */
    private LinkedList<Cost> additionalCosts = new LinkedList<>();


    /**
     * needed to predict profits from trip
     */
    private final LameTripSimulator simulator = new LameTripSimulator();

    private final double maxHours;

    private final Function<SeaTile,double[]> expectationBuilder;


    public ProfitFunction(
            double maxHours, Function<SeaTile, double[]> expectationBuilder) {
        this.maxHours = maxHours;
        this.expectationBuilder = expectationBuilder;
    }

    /**
     * compute hourly profits for this trip given current information (so if prices of fish changed since this trip is over
     * the profits computed here and the profits recorded in the trip won't be the same)
     * @param fisher agent who made the trip or anyway whose point of view matters
     * @param trip the actual trip that took place
     * @param state the model
     * @return $/hr profits of this trip
     */
    public double hourlyProfitFromThisTrip(Fisher fisher, TripRecord trip, FishState state)
    {

        double[] catches = trip.getSoldCatch();
        double earnings = 0;
        for(Species species : state.getSpecies())
            earnings += catches[species.getIndex()] * fisher.getHomePort().getMarginalPrice(species,fisher);
        double costs = 0;
        costs += oilCosts.cost(fisher,state,trip,earnings);
        for(Cost otherCost : additionalCosts)
            costs+= otherCost.cost(fisher,state,trip,earnings);

        return (earnings-costs)/ trip.getDurationInHours();
    }

    public double hourlyProfitFromHypotheticalTripHere(Fisher fisher, SeaTile where, FishState state)
    {
        return hourlyProfitFromThisTrip(fisher,
                                        simulator.simulateRecord(fisher,where,state,maxHours,expectationBuilder.apply(where)),
                                        state);
    }


    public LinkedList<Cost> getAdditionalCosts() {
        return additionalCosts;
    }
}
