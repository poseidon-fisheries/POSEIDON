package uk.ac.ox.oxfish.utility;

import com.esotericsoftware.minlog.Log;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.After;
import org.junit.Test;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.YearlyFisherTimeSeries;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaBathymetryScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 4/20/16.
 */
public class SerializeTest {

    @Test
    public void progressesCorrectly() throws Exception {
        Log.info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                         "They should have the same results");



        FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(new PrototypeScenario());
        state.start();
        for(int i=0; i<400; i++)
            state.schedule.step(state);


        XStream xstream = new XStream(new StaxDriver());
        Log.info(state.timeString());
        Log.info("Writing to file!");
        String xml = xstream.toXML(state);
        Files.write(Paths.get("save.checkpoint"),xml.getBytes());


        Log.info("Reading from File");
        xml = new String(Files.readAllBytes(Paths.get("save.checkpoint")));
        FishState state2 = (FishState) xstream.fromXML(xml);
        Log.info("Read");
        assertEquals(state.random.nextDouble(),
                     state2.random.nextDouble(),
                     .001);

        for(int i=0; i<400; i++) {
            state.schedule.step(state);
            state2.schedule.step(state2);
            //the randomizers are linked!
            assertEquals(state.random.nextDouble(),
                         state2.random.nextDouble(),
                         .001);
        }

        assertEquals(100,state.getFishers().size());
        assertEquals(100,state2.getFishers().size());
        assertEquals(800,state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(800,state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(state.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                      state2.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);

        assertEquals(state.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);


        /*
        FishGUI vid = new FishGUI(state);
        Console c = ((Console) vid.createController());
        c.doOpen();
        c.setVisible(true);
        Thread.sleep(50000);
        */
    }


    @Test
    public void randomSeedWorks() throws Exception {
        Log.info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                         "They should have the same results");


        long seed = System.currentTimeMillis();
        FishState state = new FishState(seed);
        FishState state2 = new FishState(seed);
        state.setScenario(new CaliforniaBathymetryScenario());
        state2.setScenario(new CaliforniaBathymetryScenario());
        state.start();
        state2.start();
        assertEquals(state.random.nextDouble(),
                     state2.random.nextDouble(),
                     .001);
        for(int i=0; i<20; i++) {
            state.schedule.step(state);
            state2.schedule.step(state2);
            System.out.println(i);
            assertEquals(state.random.nextDouble(),
                         state2.random.nextDouble(),
                         .001);
        }
        assertEquals(state.random.nextDouble(),
                     state2.random.nextDouble(),
                     .001);

        for(int id=0; id<state.getFishers().size(); id++)
        {
            assertEquals(state.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                         state2.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                         .001);
        }

        for(int i=0; i<20; i++) {
            //       Log.setLogger(new FishStateLogger(state,Paths.get("log1.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state.schedule.step(state);
            //      Log.trace("-----------------------------------------");
            //       Log.setLogger(new FishStateLogger(state2,Paths.get("log2.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state2.schedule.step(state2);
            //the randomizers are linked!

            Log.info("step " + i);
            for(int id=0; id<state.getFishers().size(); id++)
            {
                assertEquals(state.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                             state2.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                             .001);
            }

            assertEquals(state.random.nextDouble(),
                         state2.random.nextDouble(),
                         .001);

        }
        assertEquals(state.getFishers().size(),state2.getFishers().size());
        assertEquals(40,state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(40,state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(state.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);

        assertEquals(state.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);
    }

    @Test
    public void progressesCorrectlyCalifornia() throws Exception {
        Log.info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                         "They should have the same results");




        FishState state = new FishState(System.currentTimeMillis());
        CaliforniaBathymetryScenario scenario = new CaliforniaBathymetryScenario();
        state.setScenario(scenario);
        state.start();
        for(int i=0; i<20; i++)
            state.schedule.step(state);


        XStream xstream = new XStream(new StaxDriver());
        Log.info(state.timeString());
        Log.info("Writing to file!");
        String xml = xstream.toXML(state);
        Log.info("Reading from File");


        Files.write(Paths.get("save2.checkpoint"),xml.getBytes());
        xml = new String(Files.readAllBytes(Paths.get("save2.checkpoint")));
        FishState state2 = (FishState) xstream.fromXML(xml);
        Log.info("Read");
        assertEquals(state.random.nextDouble(),
                     state2.random.nextDouble(),
                     .001);

        for(int id=0; id<state.getFishers().size(); id++)
        {
            assertEquals(state.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                         state2.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                         .001);
        }

        for(int i=0; i<20; i++) {
     //       Log.setLogger(new FishStateLogger(state,Paths.get("log1.log")));
     //       Log.set(Log.LEVEL_TRACE);
            state.schedule.step(state);
      //      Log.trace("-----------------------------------------");
     //       Log.setLogger(new FishStateLogger(state2,Paths.get("log2.log")));
     //       Log.set(Log.LEVEL_TRACE);
            state2.schedule.step(state2);
            //the randomizers are linked!

            Log.info("step " + i);
            for(int id=0; id<state.getFishers().size(); id++)
            {
                assertEquals(state.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                             state2.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                             .001);
            }

            assertEquals(state.random.nextDouble(),
                         state2.random.nextDouble(),
                         .001);

        }
        assertEquals(state.getFishers().size(),state2.getFishers().size());
        assertEquals(40,state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(40,state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(state.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);

        assertEquals(state.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);
    }


    @Test
    public void progressesCorrectlyCaliforniaAnarchy() throws Exception {
        Log.info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                         "They should have the same results");




        FishState state = new FishState(System.currentTimeMillis());
        CaliforniaBathymetryScenario scenario = new CaliforniaBathymetryScenario();
        scenario.setRegulation(new AnarchyFactory());
        state.setScenario(scenario);
        state.start();
        for(int i=0; i<20; i++)
            state.schedule.step(state);


        XStream xstream = new XStream(new StaxDriver());
        Log.info(state.timeString());
        Log.info("Writing to file!");
        String xml = xstream.toXML(state);
        Log.info("Reading from File");


        Files.write(Paths.get("save3.checkpoint"),xml.getBytes());
        xml = new String(Files.readAllBytes(Paths.get("save3.checkpoint")));
        FishState state2 = (FishState) xstream.fromXML(xml);
        Log.info("Read");
        assertEquals(state.random.nextDouble(),
                     state2.random.nextDouble(),
                     .001);

        for(int id=0; id<state.getFishers().size(); id++)
        {
            assertEquals(state.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                         state2.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                         .001);
        }

        for(int i=0; i<20; i++) {
            //       Log.setLogger(new FishStateLogger(state,Paths.get("log1.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state.schedule.step(state);
            //      Log.trace("-----------------------------------------");
            //       Log.setLogger(new FishStateLogger(state2,Paths.get("log2.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state2.schedule.step(state2);
            //the randomizers are linked!

            for(int id=0; id<state.getFishers().size(); id++)
            {
                assertEquals(state.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                             state2.getFishers().get(id).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                             .001);
            }

            assertEquals(state.random.nextDouble(),
                         state2.random.nextDouble(),
                         .001);

        }
//aaa
        assertEquals(state.getFishers().size(),state2.getFishers().size());
        assertEquals(40,state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(40,state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(state.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getLatestYearlyObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);

        assertEquals(state.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     state2.getFishers().get(5).getDailyData().getLatestObservation(YearlyFisherTimeSeries.CASH_COLUMN),
                     .001);
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get("save.checkpoint"));
        Files.deleteIfExists(Paths.get("save2.checkpoint"));
        Files.deleteIfExists(Paths.get("save3.checkpoint"));

    }
}
