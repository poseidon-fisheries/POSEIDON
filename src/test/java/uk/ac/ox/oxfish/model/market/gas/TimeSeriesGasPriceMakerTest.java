package uk.ac.ox.oxfish.model.market.gas;

import com.beust.jcommander.internal.Lists;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.FromLeftToRightFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.IntervalPolicy;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * Created by carrknight on 7/18/17.
 */
public class TimeSeriesGasPriceMakerTest {


    @Test
    public void daily() throws Exception {


        TimeSeriesGasPriceMaker maker = new TimeSeriesGasPriceMaker(
                Lists.newArrayList(0d,1d,2d,3d,4d),
                false,
                IntervalPolicy.EVERY_STEP
        );


        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(0);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);
        state.setScenario(scenario);
        state.start();


        Port port = state.getPorts().get(0) ;
        //if asked, the initial price is 0
        assertEquals(maker.supplyInitialPrice(port.getLocation(),port.getName()),0d,.001);

        maker.start(port,state);
        state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),1d,.001);

        state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),2d,.001);

        state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),3d,.001);

        state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),4d,.001);

        //and from now on it is stuck at 4
        state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),4d,.001);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),4d,.001);



    }

    @Test
    public void fromCSV() throws Exception {

        FishState state = new FishState();
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setBiologyInitializer(new FromLeftToRightFactory());
        scenario.setFishers(0);
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(4));
        mapInitializer.setWidth(new FixedDoubleParameter(4));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);
        state.setScenario(scenario);
        state.start();


        CsvTimeSeriesGasFactory factory = new CsvTimeSeriesGasFactory();
        factory.setCsvFile(Paths.get("inputs", "california", "2001_gasprice.csv"));
        factory.setColumnNumber(1);
        factory.setHeaderInFile(true);
        factory.setLoopThroughTheCSV(false);
        factory.setScaling(1);

        TimeSeriesGasPriceMaker maker = factory.apply(state);

        Port port = state.getPorts().get(0) ;
        //if asked, the initial price is 0
        assertEquals(maker.supplyInitialPrice(port.getLocation(),port.getName()),1.677d,.001);

        maker.start(port,state);

        for(int i=0; i<365; i++)
            state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),1.561d,.001);

        for(int i=0; i<365; i++)
            state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),1.878d,.001);

        for(int i=0; i<365; i++)
            state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),2.166d,.001);

        for(int i=0; i<365; i++)
            state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),2.517d,.001);


        for(int i=0; i<365; i++)
            state.schedule.step(state);
        assertEquals(port.getGasPricePerLiter(),2.855d,.001);




    }
}