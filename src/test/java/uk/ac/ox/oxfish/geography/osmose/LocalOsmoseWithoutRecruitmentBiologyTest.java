package uk.ac.ox.oxfish.geography.osmose;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.OsmoseBiologyFactory;
import uk.ac.ox.oxfish.geography.mapmakers.OsmoseMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 3/6/17.
 */
public class LocalOsmoseWithoutRecruitmentBiologyTest {


    @Test
    public void recruitmentAgesAndBiomass() throws Exception
    {
        PrototypeScenario scenario = new PrototypeScenario();
        OsmoseBiologyFactory biologyInitializer = new OsmoseBiologyFactory();
        biologyInitializer.getRecruitmentAges().put(2,0); //demersal1 recruits immediately
        biologyInitializer.getRecruitmentAges().put(3,10000); //demersal2 never recruits!
        biologyInitializer.getRecruitmentAges().put(4,1); //demersal3 recruits after 1 year
        biologyInitializer.setPreInitializedConfiguration(false);

        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setMapInitializer(new OsmoseMapInitializerFactory());
        //speed up
        scenario.setFishers(1);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        //because there is no gap in recruitment, the two ought to be the same
        Double total = state.getDailyDataSet().getColumn("Total Biomass demersal1").getLatest();
        Double standard = state.getDailyDataSet().getColumn("Biomass demersal1").getLatest();
        assertEquals(total,standard,.001d);


        //because the recruitment is too late, there ought to be no biomass in the standard Biomass column
        total = state.getDailyDataSet().getColumn("Total Biomass demersal2").getLatest();
        standard = state.getDailyDataSet().getColumn("Biomass demersal2").getLatest();
        assertEquals(0d,standard,.0001d);
        assertTrue(total> FishStateUtilities.EPSILON);
        assertTrue(total> standard);

        //normal recruitment, the total biomass column should be higher because it contains the biomass of all the juveniles
        total = state.getDailyDataSet().getColumn("Total Biomass demersal3").getLatest();
        standard = state.getDailyDataSet().getColumn("Biomass demersal3").getLatest();
        assertTrue(standard > FishStateUtilities.EPSILON);
        assertTrue(total> standard);


    }
}