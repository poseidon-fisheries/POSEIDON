package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Using a simulator to guess the proper costs of a trip
 * Created by carrknight on 2/13/17.
 */
public class SimulatedHourlyCostExtractor implements ObservationExtractor {


    private final ProfitFunction simulator;


    public SimulatedHourlyCostExtractor(final double maxHoursOut) {
        this.simulator = new ProfitFunction(maxHoursOut);
    }

    public double extract(SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {
        TripRecord simulation = simulator.simulateTrip(agent,
                                                       agent.getGear().expectedHourlyCatch(agent, tile, 1,
                                                                                           model.getBiology()),
                                                       tile,
                                                       model
        );




        return (simulation.getTotalCosts()  + simulation.getOpportunityCosts())/simulation.getDurationInHours();
    }
}


