package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 3/23/17.
 */
public class SimulatedHourlyProfitExtractor implements ObservationExtractor {

    private final static LameTripSimulator simulator = new LameTripSimulator();
    private final double maxHoursOut;

    public SimulatedHourlyProfitExtractor(final double maxHoursOut) {
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
        if(simulation== null)
            return -10000;
        return simulation.getProfitPerHour(true);
    }
}
