/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.strategies.destination;

import com.google.common.collect.Lists;
import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by carrknight on 6/8/16.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SNALSARDestinationStrategyTest {


    @Test
    public void extractorsStart() throws Exception {


        final SNALSARDestinationFactory factory = new SNALSARDestinationFactory();
        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final SNALSARDestinationStrategy strategy = factory.apply(model);
        final Fisher test = mock(Fisher.class);

        strategy.start(model, test);
        verify(test, times(6)).addFeatureExtractor(anyString(), any());
    }

    @Test
    public void randomizesAsDefault() throws Exception {

        final SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final SNALSARDestinationStrategy strategy = factory.apply(model);
        final SeaTile option1 = mock(SeaTile.class);
        when(option1.isWater()).thenReturn(true);
        final SeaTile option2 = mock(SeaTile.class);
        when(option2.isWater()).thenReturn(true);
        final ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);

        defaultSetup(model, strategy, options);


        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for (int i = 0; i < 100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class), mock(Fisher.class, RETURNS_DEEP_STUBS));
            if (strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                Assertions.assertEquals(strategy.getFavoriteSpot(), (option2));
                timesOption2WasChosen++;
            }

        }
        Assertions.assertTrue(timesOption1WasChosen > 10);
        Assertions.assertTrue(timesOption2WasChosen > 10);

    }

    //creates an ALL PASS extractor
    public FeatureExtractors defaultSetup(
        final FishState model,
        final SNALSARDestinationStrategy strategy,
        final List<SeaTile> options
    ) {
        final Fisher fisher = mock(Fisher.class, RETURNS_DEEP_STUBS);

        strategy.start(model, fisher);
        final FeatureExtractors<SeaTile> extractors = mock(FeatureExtractors.class);
        when(fisher.getTileRepresentation()).thenReturn(extractors);
        //all safe profitable and legal
        when(extractors.extractFeature(
            eq(SNALSARutilities.SAFE_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(new FixedMap<>(1d, options));
        when(extractors.extractFeature(
            eq(SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(new FixedMap<>(1d, options));
        when(extractors.extractFeature(
            eq(SNALSARutilities.LEGAL_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(new FixedMap<>(1d, options));
        //everything good on the profits side
        when(extractors.extractFeature(
            eq(SNALSARutilities.PROFIT_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(new FixedMap<>(100d, options));
        when(extractors.extractFeature(
            eq(SNALSARutilities.FAILURE_THRESHOLD),
            anyCollection(),
            any(),
            any()
        )).thenReturn(new FixedMap<>(10d, options));
        when(extractors.extractFeature(
            eq(SNALSARutilities.ACCEPTABLE_THRESHOLD),
            anyCollection(),
            any(),
            any()
        )).thenReturn(new FixedMap<>(50d, options));

        return extractors;
    }

    @Test
    public void safeWins() throws Exception {

        final SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final SNALSARDestinationStrategy strategy = factory.apply(model);
        final SeaTile option1 = mock(SeaTile.class);
        when(option1.isWater()).thenReturn(true);
        final SeaTile option2 = mock(SeaTile.class);
        when(option2.isWater()).thenReturn(true);
        final ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);
        final FeatureExtractors extractors = defaultSetup(model, strategy, options);

        final HashMap<SeaTile, Double> safety = new HashMap<>();


        safety.put(option1, 1d);
        safety.put(option2, -1d);
        when(extractors.extractFeature(
            eq(SNALSARutilities.SAFE_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(safety);

        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for (int i = 0; i < 100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class), mock(Fisher.class, RETURNS_DEEP_STUBS));
            if (strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                Assertions.assertEquals(strategy.getFavoriteSpot(), (option2));
                timesOption2WasChosen++;
            }

        }
        Assertions.assertEquals(100, timesOption1WasChosen);
        Assertions.assertEquals(0, timesOption2WasChosen);

    }

    @Test
    public void sociallyAcceptable() throws Exception {

        final SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final SNALSARDestinationStrategy strategy = factory.apply(model);
        final SeaTile option1 = mock(SeaTile.class);
        when(option1.isWater()).thenReturn(true);
        final SeaTile option2 = mock(SeaTile.class);
        when(option2.isWater()).thenReturn(true);
        final SeaTile option3 = mock(SeaTile.class);
        when(option3.isWater()).thenReturn(true);
        final ArrayList<SeaTile> options = Lists.newArrayList(option1, option2, option3);
        final FeatureExtractors extractors = defaultSetup(model, strategy, options);

        //option 3 is unsafe
        final HashMap<SeaTile, Double> safety = new HashMap<>();
        safety.put(option1, 1d);
        safety.put(option2, 1d);
        safety.put(option3, -1d);
        when(extractors.extractFeature(
            eq(SNALSARutilities.SAFE_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(safety);

        //option 1 is socially unacceptable
        final HashMap<SeaTile, Double> acceptable = new HashMap<>();
        acceptable.put(option1, -1d);
        acceptable.put(option2, 1d);
        acceptable.put(option3, 1d);
        when(extractors.extractFeature(
            eq(SNALSARutilities.SOCIALLY_APPROPRIATE_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(acceptable);


        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for (int i = 0; i < 100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class), mock(Fisher.class, RETURNS_DEEP_STUBS));
            if (strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                Assertions.assertEquals(strategy.getFavoriteSpot(), (option2));
                timesOption2WasChosen++;
            }

        }
        Assertions.assertEquals(0, timesOption1WasChosen);
        Assertions.assertEquals(100, timesOption2WasChosen);

    }

    @Test
    public void avoidFailures() throws Exception {

        final SNALSARDestinationFactory factory = new SNALSARDestinationFactory();

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        when(model.getRandom()).thenReturn(new MersenneTwisterFast());
        final SNALSARDestinationStrategy strategy = factory.apply(model);

        final SeaTile option1 = mock(SeaTile.class);
        when(option1.isWater()).thenReturn(true);
        final SeaTile option2 = mock(SeaTile.class);
        when(option2.isWater()).thenReturn(true);
        final ArrayList<SeaTile> options = Lists.newArrayList(option1, option2);

        final FeatureExtractors extractors = defaultSetup(model, strategy, options);

        //threshold profits are 10
        final HashMap<SeaTile, Double> profits = new HashMap<>();


        profits.put(option1, 5d);
        profits.put(option2, 11d);
        when(extractors.extractFeature(
            eq(SNALSARutilities.PROFIT_FEATURE),
            anyCollection(),
            any(),
            any()
        )).thenReturn(profits);

        when(model.getMap().getAllSeaTilesExcludingLandAsList()).thenReturn(options);
        int timesOption1WasChosen = 0;
        int timesOption2WasChosen = 0;
        for (int i = 0; i < 100; i++) {
            strategy.reactToFinishedTrip(mock(TripRecord.class), mock(Fisher.class, RETURNS_DEEP_STUBS));
            if (strategy.getFavoriteSpot().equals(option1))
                timesOption1WasChosen++;
            else {
                Assertions.assertEquals(strategy.getFavoriteSpot(), (option2));
                timesOption2WasChosen++;
            }

        }
        Assertions.assertEquals(0, timesOption1WasChosen);
        Assertions.assertEquals(100, timesOption2WasChosen);

    }

    @Test
    public void cheatingEmergence() throws Exception {
        final PrototypeScenario scenario = new PrototypeScenario();
        scenario.setCheaters(true);
        scenario.getStartingMPAs().add(new StartingMPA(0, 0, 3, 15));
        //few fishers
        scenario.setFishers(25);
        //small map makes it faster
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(15));
        mapInitializer.setHeight(new FixedDoubleParameter(15));
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0));
        scenario.setMapInitializer(mapInitializer);
        //snalsar!
        final SNALSARDestinationFactory snalsar = new SNALSARDestinationFactory();
        //you need to make more than 5 to not be considered a failure!
        final FixedProfitThresholdFactory failureThreshold = new FixedProfitThresholdFactory();
        failureThreshold.setFixedThreshold(new FixedDoubleParameter(5d));
        snalsar.setFailureThreshold(failureThreshold);
        scenario.setDestinationStrategy(snalsar);

        final FishState state = new FishState();
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
        Assertions.assertEquals(0, cheaters, .001);


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
        Assertions.assertTrue(cheaters / total > .5);

    }


    /**
     * 2 species ITQ, both are valuable but the quotas of the ones only available south are very few so that
     * it's better to fish north. The results are muffled by the fact that over time north gets consumed and it becomes better
     * to fish south instead anyway.
     *
     * @throws Exception
     */
    @Test
    public void itqAffectsGeographySNALSAR() throws Exception {


        final FishYAML yaml = new FishYAML();
        final String scenarioYaml = String.join("\n", Files.readAllLines(
            Paths.get("inputs", "first_paper", "location_itq.yaml")));
        final PrototypeScenario scenario = yaml.loadAs(scenarioYaml, PrototypeScenario.class);
        //few fishers
        scenario.setFishers(25);
        //small map makes it faster
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(15));
        mapInitializer.setHeight(new FixedDoubleParameter(15));
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0));
        scenario.setMapInitializer(mapInitializer);
        scenario.setPortPositionX(5);
        scenario.setPortPositionY(7);
        //add snalsar
        scenario.setDestinationStrategy(new SNALSARDestinationFactory());
        final FishState state = new FishState();
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
        Assertions.assertTrue(towsNorth / ((double) towsNorth + towsSouth) > .6);


    }
}
