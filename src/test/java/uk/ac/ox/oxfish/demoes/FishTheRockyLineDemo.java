package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.RockyLogisticFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.HabitatAwareGearFactory;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.habitat.RockyRectanglesHabitatFactory;
import uk.ac.ox.oxfish.geography.habitat.RockyRectanglesHabitatInitializer;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertTrue;

/**
 * A lot of fishing occurs at the border much like an MPA
 * Created by carrknight on 10/5/15.
 */
public class FishTheRockyLineDemo {

    @Test
    public void fishTheRockyLine() throws Exception
    {


        double borderTows = 0;

        int rockyAreas = 0;
        FishState state;

        do {
            rockyAreas = 0;
            PrototypeScenario scenario = new PrototypeScenario();
            scenario.setHabitatInitializer(new RockyRectanglesHabitatFactory());
            HabitatAwareGearFactory gear = new HabitatAwareGearFactory();
            gear.setMeanCatchabilityRocky(new FixedDoubleParameter(0));
            scenario.setFishers(200); //this way resources are consumed faster, makes for a stronger fish the line
            //one can get reliably between 35-50% fishing the border (which is a small area anyway) just with 100
            //but there are times where a corner of the map recovers enough that temporal dips happen; that makes
            //for an ineffectual measure to test against. Instead with 200 fishers i have never seen the percentage of effort
            //spent in the border to be below 60%
            scenario.setGear(gear);
            scenario.setBiologyInitializer(new RockyLogisticFactory());
            scenario.setGridCellSizeInKm(2);


            state = new FishState(System.currentTimeMillis());
            state.setScenario(scenario);
            state.start();
            NauticalMap map = state.getMap();
            for (int x = 0; x < map.getWidth(); x++) {
                for (int y = 0; y < map.getHeight(); y++) {
                    if (map.getSeaTile(x, y).getRockyPercentage() > .9)
                        rockyAreas++;
                }
            }
        }
        while (rockyAreas < 50); //keep resetting if the map has too few rocky areas


        while (state.getYear() < 10)
            state.schedule.step(state);




        System.out.println(state.getDailyDataSet().getColumn(
                                   RockyRectanglesHabitatInitializer.BORDER_FISHING_INTENSITY).getLatest()
                                   + " -------- " +
                                   state.getDailyDataSet().getColumn(
                                           RockyRectanglesHabitatInitializer.ROCKY_FISHING_INTENSITY).getLatest()

        );

        assertTrue(state.getDailyDataSet().getColumn(
                RockyRectanglesHabitatInitializer.BORDER_FISHING_INTENSITY).getLatest()
                           > 40); //on average more than 40% of all tows happened in rocky areas
    }









}
