/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import org.junit.After;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.VariableBiomassBasedBiology;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripImitativeDestinationFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherYearlyTimeSeries;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.scenario.CaliforniaAbundanceScenario;
import uk.ac.ox.oxfish.model.scenario.DerisoCaliforniaScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * Created by carrknight on 4/20/16.
 */
public class SerializeTest {

    @Test
    public void progressesCorrectly() throws Exception {
        Logger.getGlobal()
            .info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                "They should have the same results");


        final FishState state = new FishState(System.currentTimeMillis());
        state.setScenario(new PrototypeScenario());
        state.start();
        for (int i = 0; i < 400; i++)
            state.schedule.step(state);


        final XStream xstream = new XStream(new StaxDriver());
        Logger.getGlobal().info(state.timeString());
        Logger.getGlobal().info("Writing to file!");
        String xml = xstream.toXML(state);
        Files.write(Paths.get("save.checkpoint"), xml.getBytes());


        Logger.getGlobal().info("Reading from File");
        xml = new String(Files.readAllBytes(Paths.get("save.checkpoint")));
        final FishState state2 = (FishState) xstream.fromXML(xml);
        Logger.getGlobal().info("Read");
        assertEquals(
            state.random.nextDouble(),
            state2.random.nextDouble(),
            .001
        );

        for (int i = 0; i < 400; i++) {
            state.schedule.step(state);
            state2.schedule.step(state2);
            //the randomizers are linked!
            assertEquals(
                state.random.nextDouble(),
                state2.random.nextDouble(),
                .001
            );
        }

        assertEquals(100, state.getFishers().size());
        assertEquals(100, state2.getFishers().size());
        assertEquals(800, state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(800, state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(
            state.getFishers().get(5).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(5).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );

        assertEquals(
            state.getFishers().get(5).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(5).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );


        /*
        FishGUI vid = new FishGUI(state);
        Console c = ((Console) vid.createController());
        c.doOpen();
        c.setVisible(true);
        Thread.sleep(50000);
        */
    }


    @Test
    public void randomSeedWorks1() throws Exception {

        final long seed = System.currentTimeMillis();

        final FishState state = new FishState(seed);
        final FishState state2 = new FishState(seed);
        final PrototypeScenario scenario1 = new PrototypeScenario();
        state.setScenario(scenario1);
        final PrototypeScenario scenario2 = new PrototypeScenario();
        state2.setScenario(scenario2);
        scenario1.setFishers(2);
        scenario2.setFishers(2);

        state.start();
        state2.start();


        for (int x = 0; x < state.getMap().getWidth(); x++)
            for (int y = 0; y < state.getMap().getHeight(); y++) {
                assertEquals(
                    state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                    state2.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                    .0001
                );
            }

        for (int day = 0; day < 600; day++) {
            System.out.println("day " + day);
            state.schedule.step(state);
            state2.schedule.step(state2);

            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }

            for (int x = 0; x < state.getMap().getWidth(); x++)
                for (int y = 0; y < state.getMap().getHeight(); y++) {
                    assertEquals(
                        state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                        state2.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                        .0001
                    );
                }

        }
    }

    @Test
    public void randomSeedWorksDeriso() throws Exception {

        final long seed = System.currentTimeMillis();

        System.out.println("seed " + seed);
        final FishState state = new FishState(seed);
        final FishState state2 = new FishState(seed);
        final DerisoCaliforniaScenario scenario1 = new DerisoCaliforniaScenario();
        state.setScenario(scenario1);
        final DerisoCaliforniaScenario scenario2 = new DerisoCaliforniaScenario();
        state2.setScenario(scenario2);
        scenario1.setMainDirectory(Paths.get("inputs", "california"));
        scenario1.setPortFileName("no_ports.csv");
        scenario1.setDerisoFileNames("deriso_2007.yaml");
        scenario2.setMainDirectory(Paths.get("inputs", "california"));
        scenario2.setDerisoFileNames("deriso_2007.yaml");
        scenario2.setPortFileName("no_ports.csv");

        state.start();
        state2.start();


        for (int day = 0; day < 600; day++) {
            System.out.println("day " + day);
            state.schedule.step(state);
            state2.schedule.step(state2);


            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }

            for (int species = 0; species < state.getSpecies().size(); species++)
                for (int x = 0; x < state.getMap().getWidth(); x++)
                    for (int y = 0; y < state.getMap().getHeight(); y++) {
                        if (state.getMap().getSeaTile(x, y).isFishingEvenPossibleHere()) {
                            assertEquals(
                                ((VariableBiomassBasedBiology) state.getMap().getSeaTile(
                                    x,
                                    y
                                ).getBiology()).getCarryingCapacity(
                                    species),
                                ((VariableBiomassBasedBiology) state2.getMap().getSeaTile(
                                    x,
                                    y
                                ).getBiology()).getCarryingCapacity(
                                    species),
                                .0001
                            );
                            assertEquals(
                                state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(species)),
                                state2.getMap().getSeaTile(x, y).getBiomass(state2.getSpecies().get(species)),
                                .0001
                            );
                        }
                    }
        }
    }


    @Test
    public void randomSeedWorksYaml() throws Exception {

        final long seed = System.currentTimeMillis();

        final FishYAML yaml = new FishYAML();

        final FishState state = new FishState(seed);
        final FishState state2 = new FishState(seed);
        FileReader io = new FileReader(Paths.get("inputs", "YAML Samples", "scenario", "Abstract.yaml").toFile());
        final PrototypeScenario scenario1 = yaml.loadAs(
            io,
            PrototypeScenario.class
        );
        state.setScenario(scenario1);
        io = new FileReader(Paths.get("inputs", "YAML Samples", "scenario", "Abstract.yaml").toFile());
        final PrototypeScenario scenario2 = yaml.loadAs(
            io,
            PrototypeScenario.class
        );
        state2.setScenario(scenario2);
        scenario1.setFishers(2);
        scenario2.setFishers(2);

        state.start();
        state2.start();


        for (int x = 0; x < state.getMap().getWidth(); x++)
            for (int y = 0; y < state.getMap().getHeight(); y++) {
                assertEquals(
                    state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                    state2.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                    .0001
                );
                System.out.println(state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)));
                System.out.println(state2.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)));
            }

        for (int day = 0; day < 600; day++) {
            System.out.println("day " + day);
            state.schedule.step(state);
            state2.schedule.step(state2);

            assertEquals(
                state.random.nextDouble(),
                state2.random.nextDouble(),
                .001
            );

            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }

            for (int x = 0; x < state.getMap().getWidth(); x++)
                for (int y = 0; y < state.getMap().getHeight(); y++) {
                    assertEquals(
                        state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                        state2.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(0)),
                        .0001
                    );
                }

        }
    }


    @Test
    public void randomSeedWorksCalifornia() throws Exception {
        Logger.getGlobal()
            .info("starts the california model, writes it down, reads it and runs it against the original copy. " +
                "They should have the same results");


        final long seed = System.currentTimeMillis();
        final FishState state = new FishState(seed);
        final FishState state2 = new FishState(seed);
        state.setScenario(new CaliforniaAbundanceScenario());

        ((CaliforniaAbundanceScenario) state.getScenario()).setDestinationStrategy(new PerTripImitativeDestinationFactory());
        ((CaliforniaAbundanceScenario) state.getScenario()).setPortFileName("one_port.csv");
        state2.setScenario(new CaliforniaAbundanceScenario());
        ((CaliforniaAbundanceScenario) state2.getScenario()).setDestinationStrategy(new PerTripImitativeDestinationFactory());
        ((CaliforniaAbundanceScenario) state2.getScenario()).setPortFileName("one_port.csv");

        state.start();
        state2.start();
        assertEquals(
            state.random.nextDouble(),
            state2.random.nextDouble(),
            .001
        );
        for (int i = 0; i < 20; i++) {
            state.schedule.step(state);
            state2.schedule.step(state2);
            System.out.println(i);
            assertEquals(
                state.random.nextDouble(),
                state2.random.nextDouble(),
                .001
            );


            for (int species = 0; species < 5; species++) {
                for (int x = 0; x < state.getMap().getWidth(); x++)
                    for (int y = 0; y < state.getMap().getHeight(); y++) {
                        if (state.getMap().getSeaTile(x, y).isFishingEvenPossibleHere() && !state.getSpecies()
                            .get(species)
                            .isImaginary())
                            assertArrayEquals(
                                state.getMap().getSeaTile(x, y).getAbundance(
                                    state.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE],
                                state2.getMap().getSeaTile(x, y).getAbundance(
                                    state2.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE],
                                .0001
                            );


                        assertEquals(
                            "first: " + Arrays.toString(
                                state.getMap().getSeaTile(x, y).getAbundance(
                                    state.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE]) + "\n" +
                                "second: " + Arrays.toString(
                                state2.getMap().getSeaTile(x, y).getAbundance(
                                    state2.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE]) + "\n"
                            ,
                            state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(species)),
                            state2.getMap().getSeaTile(x, y).getBiomass(state2.getSpecies().get(species)),
                            .0001
                        );
                    }
            }
            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }
        }
        assertEquals(
            state.random.nextDouble(),
            state2.random.nextDouble(),
            .001
        );


        for (int i = 0; i < 800; i++) {
            //       Log.setLogger(new FishStateLogger(state,Paths.get("log1.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state.schedule.step(state);
            //      Log.trace("-----------------------------------------");
            //       Log.setLogger(new FishStateLogger(state2,Paths.get("log2.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state2.schedule.step(state2);
            //the randomizers are linked!

            Logger.getGlobal().info("step " + i);

            for (int species = 0; species < 5; species++) {
                for (int x = 0; x < state.getMap().getWidth(); x++)
                    for (int y = 0; y < state.getMap().getHeight(); y++) {
                        if (state.getMap().getSeaTile(x, y).isFishingEvenPossibleHere() && !state.getSpecies()
                            .get(species)
                            .isImaginary())
                            assertArrayEquals(
                                state.getMap().getSeaTile(x, y).getAbundance(
                                    state.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE],
                                state2.getMap().getSeaTile(x, y).getAbundance(
                                    state2.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE],
                                .0001
                            );


                        assertEquals(
                            "first: " + Arrays.toString(
                                state.getMap().getSeaTile(x, y).getAbundance(
                                    state.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE]) + "\n" +
                                "second: " + Arrays.toString(
                                state2.getMap().getSeaTile(x, y).getAbundance(
                                    state2.getSpecies().get(species)).asMatrix()[FishStateUtilities.MALE]) + "\n"
                            ,
                            state.getMap().getSeaTile(x, y).getBiomass(state.getSpecies().get(species)),
                            state2.getMap().getSeaTile(x, y).getBiomass(state2.getSpecies().get(species)),
                            .0001
                        );
                    }
            }

            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }

            assertEquals(
                state.random.nextDouble(),
                state2.random.nextDouble(),
                .001
            );

        }
        assertEquals(state.getFishers().size(), state2.getFishers().size());
        assertEquals(820, state.getFishers().get(0).getDailyData().numberOfObservations());
        assertEquals(820, state2.getFishers().get(0).getDailyData().numberOfObservations());

        assertEquals(
            state.getFishers().get(0).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(0).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );

        assertEquals(
            state.getFishers().get(0).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(0).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );
    }

    @Test
    public void progressesCorrectlyCalifornia() throws Exception {
        Logger.getGlobal()
            .info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                "They should have the same results");


        final FishState state = new FishState(System.currentTimeMillis());
        final CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();
        scenario.setDestinationStrategy(new PerTripImitativeDestinationFactory());
        state.setScenario(scenario);
        state.start();
        for (int i = 0; i < 20; i++)
            state.schedule.step(state);


        final XStream xstream = new XStream(new StaxDriver());
        Logger.getGlobal().info(state.timeString());
        Logger.getGlobal().info("Writing to file!");
        String xml = xstream.toXML(state);
        Logger.getGlobal().info("Reading from File");


        Files.write(Paths.get("save2.checkpoint"), xml.getBytes());
        xml = new String(Files.readAllBytes(Paths.get("save2.checkpoint")));
        final FishState state2 = (FishState) xstream.fromXML(xml);
        Logger.getGlobal().info("Read");
        assertEquals(
            state.random.nextDouble(),
            state2.random.nextDouble(),
            .001
        );

        for (int id = 0; id < state.getFishers().size(); id++) {
            assertEquals(
                state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                .001
            );
        }

        for (int i = 0; i < 20; i++) {
            //       Log.setLogger(new FishStateLogger(state,Paths.get("log1.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state.schedule.step(state);
            //      Log.trace("-----------------------------------------");
            //       Log.setLogger(new FishStateLogger(state2,Paths.get("log2.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state2.schedule.step(state2);
            //the randomizers are linked!

            Logger.getGlobal().info("step " + i);
            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }

            assertEquals(
                state.random.nextDouble(),
                state2.random.nextDouble(),
                .001
            );

        }
        assertEquals(state.getFishers().size(), state2.getFishers().size());
        assertEquals(40, state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(40, state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(
            state.getFishers().get(5).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(5).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );

        assertEquals(
            state.getFishers().get(5).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(5).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );
    }


    @Test
    public void progressesCorrectlyCaliforniaAnarchy() throws Exception {
        Logger.getGlobal()
            .info("starts the abstract model, writes it down, reads it and runs it against the original copy. " +
                "They should have the same results");


        final FishState state = new FishState(System.currentTimeMillis());
        final CaliforniaAbundanceScenario scenario = new CaliforniaAbundanceScenario();
        scenario.setDestinationStrategy(new PerTripImitativeDestinationFactory());
        scenario.setRegulationPreReset(new AnarchyFactory());
        state.setScenario(scenario);
        state.start();
        for (int i = 0; i < 20; i++)
            state.schedule.step(state);


        final XStream xstream = new XStream(new StaxDriver());
        Logger.getGlobal().info(state.timeString());
        Logger.getGlobal().info("Writing to file!");
        String xml = xstream.toXML(state);
        Logger.getGlobal().info("Reading from File");


        Files.write(Paths.get("save3.checkpoint"), xml.getBytes());
        xml = new String(Files.readAllBytes(Paths.get("save3.checkpoint")));
        final FishState state2 = (FishState) xstream.fromXML(xml);
        Logger.getGlobal().info("Read");
        assertEquals(
            state.random.nextDouble(),
            state2.random.nextDouble(),
            .001
        );

        for (int id = 0; id < state.getFishers().size(); id++) {
            assertEquals(
                state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                .001
            );
        }

        for (int i = 0; i < 20; i++) {
            //       Log.setLogger(new FishStateLogger(state,Paths.get("log1.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state.schedule.step(state);
            //      Log.trace("-----------------------------------------");
            //       Log.setLogger(new FishStateLogger(state2,Paths.get("log2.log")));
            //       Log.set(Log.LEVEL_TRACE);
            state2.schedule.step(state2);
            //the randomizers are linked!

            for (int id = 0; id < state.getFishers().size(); id++) {
                assertEquals(
                    state.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    state2.getFishers().get(id).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
                    .001
                );
            }

            assertEquals(
                state.random.nextDouble(),
                state2.random.nextDouble(),
                .001
            );

        }
//aaa
        assertEquals(state.getFishers().size(), state2.getFishers().size());
        assertEquals(40, state.getFishers().get(5).getDailyData().numberOfObservations());
        assertEquals(40, state2.getFishers().get(5).getDailyData().numberOfObservations());

        assertEquals(
            state.getFishers().get(5).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(5).getLatestYearlyObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );

        assertEquals(
            state.getFishers().get(5).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            state2.getFishers().get(5).getDailyData().getLatestObservation(FisherYearlyTimeSeries.CASH_COLUMN),
            .001
        );
    }

    @After
    public void tearDown() throws Exception {
        Files.deleteIfExists(Paths.get("save.checkpoint"));
        Files.deleteIfExists(Paths.get("save2.checkpoint"));
        Files.deleteIfExists(Paths.get("save3.checkpoint"));

    }
}
