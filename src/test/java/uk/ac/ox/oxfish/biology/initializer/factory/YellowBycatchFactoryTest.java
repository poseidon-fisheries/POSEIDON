package uk.ac.ox.oxfish.biology.initializer.factory;

import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FishStateUtilities;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 3/13/17.
 */
public class YellowBycatchFactoryTest {


    //with no fishing, the default parameters ought to keep fishery constant


    @Test
    public void noFishing() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(10d));
        mapInitializer.setWidth(new FixedDoubleParameter(10d));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1d));
        scenario.setMapInitializer(mapInitializer);


        YellowBycatchFactory biologyInitializer = new YellowBycatchFactory();
        biologyInitializer.setVerticalSeparator(new FixedDoubleParameter(5));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setFishers(0);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        double initialTarget = state.getTotalBiomass(state.getSpecies().get(0));
        double initialBycatch = state.getTotalBiomass(state.getSpecies().get(1));
        while(state.getYear()<4)
            state.schedule.step(state);

        assertEquals(initialBycatch,state.getTotalBiomass(state.getSpecies().get(1)),1000);
        assertEquals(initialTarget,state.getTotalBiomass(state.getSpecies().get(0)),1000);
    }



    @Test
    public void tanksThenRecovers() throws Exception {

        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(10d));
        mapInitializer.setWidth(new FixedDoubleParameter(10d));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1d));
        scenario.setMapInitializer(mapInitializer);


        YellowBycatchFactory biologyInitializer = new YellowBycatchFactory();
        biologyInitializer.setVerticalSeparator(new FixedDoubleParameter(5));
        scenario.setBiologyInitializer(biologyInitializer);
        scenario.setFishers(100);

        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(scenario);
        state.start();
        double initialTarget = state.getTotalBiomass(state.getSpecies().get(0));
        double initialBycatch = state.getTotalBiomass(state.getSpecies().get(1));
        while(state.getYear()<2)
            state.schedule.step(state);

        double intermediateTarget = state.getTotalBiomass(state.getSpecies().get(0));
        double intermediateBycatch = state.getTotalBiomass(state.getSpecies().get(1));
        assertTrue(initialBycatch> intermediateBycatch + FishStateUtilities.EPSILON);
        assertTrue(initialTarget> intermediateTarget + FishStateUtilities.EPSILON);

        //now ban fishing
        FishingSeason regulation = new FishingSeason(true, 0);
        for(Fisher fisher : state.getFishers()) {
            fisher.setRegulation(regulation);
        }
        while(state.getYear()<4)
            state.schedule.step(state);
        assertTrue(state.getTotalBiomass(state.getSpecies().get(1))> intermediateBycatch + FishStateUtilities.EPSILON);
        assertTrue(state.getTotalBiomass(state.getSpecies().get(0))> intermediateTarget + FishStateUtilities.EPSILON);


    }





}