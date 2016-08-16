package uk.ac.ox.oxfish.fisher.selfanalysis.profit;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.LameTripSimulator;
import uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 8/16/16.
 */
public class ProfitFunctionTest {


    @Test
    public void attached() throws Exception {

        //if I attach it to a fisher in a real simulation it should compute precisely the profits
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        MaximumStepsFactory fishingStrategy = new MaximumStepsFactory();
        scenario.setFishingStrategy(fishingStrategy);
        state.start();


        ProfitFunction function = new ProfitFunction(new LameTripSimulator(),24*5);

        final Fisher fisher = state.getFishers().get(0);
        fisher.addTripListener(
                new TripListener() {
                    @Override
                    public void reactToFinishedTrip(TripRecord record) {
                        System.out.println("day : " + state.getDay());
                        TripRecord simulated = function.simulateTrip(fisher,
                                                                     new double[]{record.getSoldCatch()[0] / record.getEffort()},
                                                                     record.getMostFishedTileInTrip(),
                                                                     state
                                                                     );

                        assertEquals(simulated.getDistanceTravelled(),record.getDistanceTravelled(),.001d);
                        assertEquals(record.getEffort()+record.getDistanceTravelled()/fisher.getBoat().getSpeedInKph() - record.getDurationInHours(),0,.1d);
                        assertEquals(simulated.getEffort(),record.getEffort(),.001d);
                        assertEquals(simulated.getLitersOfGasConsumed(),record.getLitersOfGasConsumed(),.001d);
                        assertEquals(simulated.getDurationInHours(),record.getDurationInHours(),.1);
                        double hourlyProfits = function.hourlyProfitFromHypotheticalTripHere(
                                fisher, record.getMostFishedTileInTrip(),
                                state,
                                new Function<SeaTile, double[]>() {
                                    @Override
                                    public double[] apply(SeaTile seaTile) {
                                        return new double[]{record.getSoldCatch()[0] / record.getEffort()};
                                    }
                                },
                                false


                        );
                        assertEquals(hourlyProfits,record.getProfitPerHour(true),.1);

                    }
                }
        );

        for(int i=0; i<10000; i++)
            state.schedule.step(state);


    }

}