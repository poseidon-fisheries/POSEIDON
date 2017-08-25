package uk.ac.ox.oxfish.fisher.heatmap.regression.extractors;

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;

/**
 * Created by carrknight on 3/23/17.
 */
public class SimulatedHourlyProfitExtractor implements ObservationExtractor {

    private final ProfitFunction simulator;

    public SimulatedHourlyProfitExtractor(final double maxHoursOut) {
        simulator= new ProfitFunction(new LameTripSimulator(),maxHoursOut);
    }


    @Override
    public double extract(
            SeaTile tile, double timeOfObservation, Fisher agent, FishState model) {


        Double hourlyProfits = simulator.simulateHourlyProfits(agent,
                                                               agent.getGear().expectedHourlyCatch(agent, tile, 1,
                                                                                                   model.getBiology()),
                                                               tile,
                                                               model,
                                                               false);
        if(!Double.isFinite(hourlyProfits))
            return -10000;
        else
            return hourlyProfits;
    }
}
