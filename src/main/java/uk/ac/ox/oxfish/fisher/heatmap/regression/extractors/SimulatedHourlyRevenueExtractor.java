package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Use a simulator (and perfect knowledge) to check how much $/hr one would make (ignoring costs)
 * Created by carrknight on 2/13/17.
 */
public class SimulatedHourlyRevenueExtractor implements ObservationExtractor {


    private final static LameTripSimulator simulator = new LameTripSimulator();
    private final double maxHoursOut;

    public SimulatedHourlyRevenueExtractor(final double maxHoursOut) {
        this.maxHoursOut = maxHoursOut;
    }


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {


        TripRecord simulation = simulator.simulateRecord(agent,
                                                         tile,
                                                         model,
                                                         maxHoursOut,
                                                         //using perfect knowledge!
                                                         agent.getGear().expectedHourlyCatch(agent, tile, 1,
                                                                                             model.getBiology()));
        return simulation.getEarnings()/simulation.getDurationInHours();
    }
}
