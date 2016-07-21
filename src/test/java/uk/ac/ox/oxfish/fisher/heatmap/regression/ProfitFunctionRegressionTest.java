package uk.ac.ox.oxfish.fisher.heatmap.regression;

import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.fisher.heatmap.regression.factory.NearestNeighborTransductionFactory;
import uk.ac.ox.oxfish.fisher.log.TripListener;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.ProfitFunction;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 7/21/16.
 */
public class ProfitFunctionRegressionTest {


    @Test
    public void regressionTest() throws Exception {
        Log.info("Makes sure that the catches/hr regressed are correct!");



        FishState state = new FishState(System.currentTimeMillis());

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(1);
        FromLeftToRightFactory biologyInitializer = new FromLeftToRightFactory();
        biologyInitializer.setBiologySmoothingIndex(new FixedDoubleParameter(0d));
        scenario.setBiologyInitializer(biologyInitializer);
        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(.01));
        scenario.setGear(gear);

        state.setScenario(scenario);
        state.start();

        ProfitFunctionRegression regression = new ProfitFunctionRegression(
                new ProfitFunction(24*5),
                new NearestNeighborTransductionFactory(),
                state
        );
        Fisher fisher = state.getFishers().get(0);
        fisher.addTripListener(new TripListener() {
            @Override
            public void reactToFinishedTrip(TripRecord record) {
                regression.addObservation(new GeographicalObservation<>(record.getMostFishedTileInTrip(),
                                                                        state.getHoursSinceStart(),
                                                                        record),fisher
                                          );
            }
        });


        //make him go to 20,20
        SeaTile target = state.getMap().getSeaTile(20, 20);
        fisher.setDestinationStrategy(new FavoriteDestinationStrategy(target));
        for(int day=0; day<10; day++)
            state.schedule.step(state);
        double predictedCatchesPerHour = regression.apply(state.getMap().getSeaTile(20, 20))[0];
        assertEquals(predictedCatchesPerHour,
                     target.getBiomass(state.getSpecies().get(0)) * .01,.001);

    }
}