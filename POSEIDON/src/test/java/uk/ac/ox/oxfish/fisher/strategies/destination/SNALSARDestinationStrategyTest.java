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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.erotetic.FeatureExtractors;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.SNALSARutilities;
import uk.ac.ox.oxfish.fisher.erotetic.snalsar.factory.FixedProfitThresholdFactory;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.SNALSARDestinationFactory;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.mpa.StartingMPA;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.FixedMap;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/8/16.
 */
public class SNALSARDestinationStrategyTest {


    @Test
    public void extractorsStart() throws Exception {


        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();
        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        SNALSARDestinationStrategy strategy = factory.apply(model);
        Fisher test = mock(Fisher.class);

        strategy.start(model,test);
        verify(test,times(6)).addFeatureExtractor(anyString(),any());
    }

    @Test
    public void randomizesAsDefault() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);
        SeaTile option1 = mock(SeaTile.class); when(option1.isWater()).thenReturn(true);
        SeaTile option2 = mock(SeaTile.class); when(option2.isWater()).thenReturn(true);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);

        defaultSetup(model, strategy,options);



        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class),mock(Fisher.class,RETURNS_DEEP_STUBS));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen>10);
        assertTrue(timesOption2WasChosen>10);

    }

    @Test
    public void safeWins() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);
        SeaTile option1 = mock(SeaTile.class); when(option1.isWater()).thenReturn(true);
        SeaTile option2 = mock(SeaTile.class); when(option2.isWater()).thenReturn(true);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);
        FeatureExtractors extractors = defaultSetup(model, strategy,options);

        HashMap<SeaTile,Double> safety = new HashMap<>();


        safety.put(option1,1d);
        safety.put(option2,-1d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.SAFE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(safety);

        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class), mock(Fisher.class,RETURNS_DEEP_STUBS));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen==100);
        assertTrue(timesOption2WasChosen==0);

    }




    @Test
    public void sociallyAcceptable() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);
        SeaTile option1 = mock(SeaTile.class); when(option1.isWater()).thenReturn(true);
        SeaTile option2 = mock(SeaTile.class); when(option2.isWater()).thenReturn(true);
        SeaTile option3 = mock(SeaTile.class); when(option3.isWater()).thenReturn(true);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2,option3);
        FeatureExtractors extractors = defaultSetup(model, strategy,options);

        //option 3 is unsafe
        HashMap<SeaTile,Double> safety = new HashMap<>();
        safety.put(option1,1d);
        safety.put(option2,1d);
        safety.put(option3,-1d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.SAFE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(safety);

        //option 1 is socially unacceptable
        HashMap<SeaTile,Double> acceptable = new HashMap<>();
        acceptable.put(option1,-1d);
        acceptable.put(option2,1d);
        acceptable.put(option3,1d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(acceptable);


        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class),mock(Fisher.class,RETURNS_DEEP_STUBS));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen==0);
        assertTrue(timesOption2WasChosen==100);

    }

    @Test
    public void avoidFailures() throws Exception {

        SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        SNALSARDestinationStrategy strategy = factory.apply(model);

        SeaTile option1 = mock(SeaTile.class); when(option1.isWater()).thenReturn(true);
        SeaTile option2 = mock(SeaTile.class); when(option2.isWater()).thenReturn(true);
        ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);

        FeatureExtractors extractors = defaultSetup(model, strategy,options);

        //threshold profits are 10
        HashMap<SeaTile,Double> profits = new HashMap<>();


        profits.put(option1,5d);
        profits.put(option2,11d);
        when(extractors.extractFeature(
                eq(SNALSARutilities.PROFIT_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(profits);

        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for(int i=0; i<100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class), mock(Fisher.class,RETURNS_DEEP_STUBS));
            if(strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                assertEquals(strategy.getFavoriteSpot(),(option2));
                timesOption2WasChosen++;
            }

        }
        assertTrue(timesOption1WasChosen==0);
        assertTrue(timesOption2WasChosen==100);

    }




    //creates an ALL PASS extractor
    public FeatureExtractors defaultSetup(FishState model,
                                          SNALSARDestinationStrategy strategy,
                                          List<SeaTile> options) {
        Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        strategy.start(model,fisher);
        FeatureExtractors<SeaTile> extractors = mock(FeatureExtractors.class);
        when(fisher.getTileRepresentation()).thenReturn(extractors);
        //all safe profitable and legal
        when(extractors.extractFeature(
                eq(SNALSARutilities.SAFE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(1d, options));
        when(extractors.extractFeature(
                eq(SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(1d,options ));
        when(extractors.extractFeature(
                eq(SNALSARutilities.LEGAL_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(1d, options));
        //everything good on the profits side
        when(extractors.extractFeature(
                eq(SNALSARutilities.PROFIT_FEATURE),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(100d,options ));
        when(extractors.extractFeature(
                eq(SNALSARutilities.FAILURE_THRESHOLD),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(10d,options ));
        when(extractors.extractFeature(
                eq(SNALSARutilities.ACCEPTABLE_THRESHOLD),
                anyCollection(),
                any(),
                any())).thenReturn(new FixedMap<>(50d,options ));

        return extractors;
    }

    @Test
    public void cheatingEmergence() throws Exception {
        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setCheaters(true);
        scenario.getStartingMPAs().add(new StartingMPA(0, 0, 3, 15));
        //few fishers
        scenario.setFishers(25);
        //small map makes it faster
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(15));
        mapInitializer.setHeight(new FixedDoubleParameter(15));
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0));
        scenario.setMapInitializer(mapInitializer);
        //snalsar!
        SNALSARDestinationFactory snalsar = new SNALSARDestinationFactory();
        //you need to make more than 5 to not be considered a failure!
        FixedProfitThresholdFactory failureThreshold = new FixedProfitThresholdFactory();
        failureThreshold.setFixedThreshold(new FixedDoubleParameter(5d));
        snalsar.setFailureThreshold(failureThreshold);
        scenario.setDestinationStrategy(snalsar);

        FishState state = new FishState();
        state.setScenario(scenario);


        state.start();
        //skip first 10 random days
        for (int day = 0; day < 10; day++)

            state.schedule.step(state);

        //fort the next 50 days nobody ought to be cheating
        double cheaters = 0;
        for (int day = 0; day < 50; day++) {
            state.schedule.step(state);
            for (int x = 0; x < 5; x++)
                for (int y = 0; y < 12; y++)
                    cheaters += state.getMap().getDailyTrawlsMap().get(0, y);

        }
        assertEquals(0, cheaters, .001);


        //after 1 year they are all (> 50%) cheating
        while (state.getYear() < 1)
            state.schedule.step(state);

        cheaters = 0;
        double total = 0;
        for (int day = 0; day < 50; day++) {
            state.schedule.step(state);
            for (int x = 0; x < 5; x++)
                for (int y = 0; y < 12; y++)
                    cheaters += state.getMap().getDailyTrawlsMap().get(0, y);
            total += Arrays.stream(state.getMap().getDailyTrawlsMap().toArray()).sum();

        }
        assertTrue(cheaters / total > .5);

    }


    /**
     * 2 species ITQ, both are valuable but the quotas of the ones only available south are very few so that
     * it's better to fish north. The results are muffled by the fact that over time north gets consumed and it becomes better
     * to fish south instead anyway.
     * @throws Exception
     */
    @Test
    public void itqAffectsGeographySNALSAR() throws Exception {



        FishYAML yaml = new FishYAML();
        String scenarioYaml = String.join("\n", Files.readAllLines(
                Paths.get("inputs", "first_paper", "location_itq.yaml")));
        PrototypeScenario scenario =  yaml.loadAs(scenarioYaml,PrototypeScenario.class);
        //few fishers
        scenario.setFishers(25);
        //small map makes it faster
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(15));
        mapInitializer.setHeight(new FixedDoubleParameter(15));
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0));
        scenario.setMapInitializer(mapInitializer);
        scenario.setPortPositionX(5);
        scenario.setPortPositionY(7);
        //add snalsar
        scenario.setDestinationStrategy(new SNALSARDestinationFactory());
        FishState state = new FishState();
        state.setScenario(scenario);





        long towsNorth = 0;
        long towsSouth = 0;

        state.start();
        //first year, just lspiRun: there is no ITQ running anyway
        while (state.getYear() < 1) {
            state.schedule.step(state);
        }
        state.schedule.step(state);


        while (state.getYear() < 2) {
            state.schedule.step(state);
            for (int x = 0; x < 15; x++) {
                for (int y = 0; y <= 6; y++) {
                    towsNorth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
                for (int y = 7; y < 15; y++) {
                    towsSouth += state.getMap().getDailyTrawlsMap().get(x, y);
                }
            }
        }

        System.out.println("North vs South : " + towsNorth / ((double) towsNorth + towsSouth));
        Assert.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .6);



    }
}