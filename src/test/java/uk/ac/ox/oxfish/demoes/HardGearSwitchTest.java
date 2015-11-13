package uk.ac.ox.oxfish.demoes;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.WellMixedBiologyFactory;
import uk.ac.ox.oxfish.experiments.HardGearSwitch;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.AbstractMarket;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by carrknight on 11/12/15.
 */
public class HardGearSwitchTest {


    @Test
    public void prototypeWorldHardGearSwitch() throws Exception {
        WellMixedBiologyFactory biologyInitializer = new WellMixedBiologyFactory();
        biologyInitializer.setFirstSpeciesCapacity(new FixedDoubleParameter(5000));
        biologyInitializer.setCapacityRatioSecondToFirst(new FixedDoubleParameter(1d));
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();

        FishState model = HardGearSwitch.buildHardSwitchGearDemo(biologyInitializer, mapInitializer, 0, 1, 500, 4500);

        //sanity check: you either catch 2 or 3
        /*
        model.registerStartable(new Startable() {
            @Override
            public void start(FishState model) {
                model.scheduleEveryDay(new Steppable() {
                    @Override
                    public void step(SimState simState)
                    {

                        for(Fisher fisher : model.getFishers())
                        {

                            assertTrue(Double.isNaN(fisher.predictDailyCatches(0)) ^
                                                      (fisher.predictDailyCatches(
                                                              0) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                              1) > FishStateUtilities.EPSILON) ^
                                                      (fisher.predictDailyCatches(
                                                              0) < FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                              1) < FishStateUtilities.EPSILON) ^
                                                      (fisher.predictDailyCatches(
                                                              0) > FishStateUtilities.EPSILON && fisher.predictDailyCatches(
                                                              1) < FishStateUtilities.EPSILON));





                        }
                    }
                }, StepOrder.AFTER_DATA);
            }

            @Override
            public void turnOff() {

            }
        });
        */
        model.start();


        while(model.getYear()<15)
            model.schedule.step(model);

        Double zeroCatchers = model.getLatestYearlyObservation("Species " + 0 + " Catchers");
        Double firstCatchers = model.getLatestYearlyObservation("Species " + 1 + " Catchers");

        assertTrue(zeroCatchers <100); //not everybody is catching 0
        assertTrue(zeroCatchers > 0); //not everybody is catching 0
        assertTrue(firstCatchers <100); //not everybody is catching 1
        assertTrue(firstCatchers > 0); //not everybody is catching 1
        assertEquals(firstCatchers+zeroCatchers,100,.00001); //not everybody is catching 1
        double firstQuotaEfficiency = model.getLatestYearlyObservation(
                model.getSpecies().get(0) + " " + AbstractMarket.LANDINGS_COLUMN_NAME) / (500 * 100);
        double secondQuotaEfficiency = model.getLatestYearlyObservation(
                model.getSpecies().get(1) + " " + AbstractMarket.LANDINGS_COLUMN_NAME) / (4500 * 100);

        System.out.println(firstQuotaEfficiency + " ------ " + secondQuotaEfficiency);
        assertTrue(firstQuotaEfficiency <=1 + FishStateUtilities.EPSILON);
        assertTrue(firstQuotaEfficiency >.8);
        assertTrue(secondQuotaEfficiency <=1+ FishStateUtilities.EPSILON);
        assertTrue(secondQuotaEfficiency >.8);



    }



}
