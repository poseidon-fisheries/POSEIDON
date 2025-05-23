/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AggregatingFad;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.Mockito.*;

public class ExogenousFadSetterFromDataTest {

    @SuppressWarnings("rawtypes")
    @Test
    public void choosesTheCorrectFADs() {

        // 2 observed sets: the first landing 100,100 the second landing 10,10
        final FadSetObservation firstObservation = new FadSetObservation(
            new Coordinate(0.5, 0.5),
            new double[]{100, 100},
            123
        );
        final FadSetObservation secondObservation = new FadSetObservation(
            new Coordinate(0.5, 0.5),
            new double[]{10, 10},
            123
        );
        final HashMap<Integer, List<FadSetObservation>> dataset = new HashMap<>();
        dataset.put(123, Lists.newArrayList(firstObservation, secondObservation));
        final ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dataset);

        final Species first = mock(Species.class);
        when(first.getIndex()).thenReturn(0);
        final Species second = mock(Species.class);
        when(second.getIndex()).thenReturn(1);
        // 4 simulated fads in the water: holding 10,10;20,20;30,30;40,40 biomass
        final Bag localFads = new Bag();
        for (int i = 1; i <= 4; i++) {
            final AggregatingFad fad = mock(AggregatingFad.class, RETURNS_DEEP_STUBS);
            final LocalBiology biology = mock(LocalBiology.class);
            when(fad.getBiology()).thenReturn(biology);
            when(biology.getBiomass(first)).thenReturn(i * 10d);
            when(biology.getBiomass(second)).thenReturn(i * 10d);
            localFads.add(fad);
        }

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);

        when(model.getSpecies()).thenReturn(Lists.newArrayList(first, second));
        setter.start(model);
        when(model.getDay()).thenReturn(123);
        when(model.getFadMap().fadsAt(any())).thenReturn(localFads);
        setter.step(model);

        // there should have been 2 matches, not out of bounds, and the error ought to be sqrt(2(100-40)^2) since second
        // fad has perfect match!
        Assertions.assertEquals(2, setter.getCounter().getColumn("Matches"), 0.0001);
        Assertions.assertEquals(0, setter.getCounter().getColumn("Failed Matches"), 0.0001);
        Assertions.assertEquals(0, setter.getCounter().getColumn("Out of Bounds"), 0.0001);
        Assertions.assertEquals(Math.sqrt(2 * Math.pow(100 - 40, 2)), setter.getCounter().getColumn("Error"), 0.0001);

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void twoObservationsOneFad() {

        // 2 observed sets: the first landing 100,100 the second landing 10,10
        final FadSetObservation firstObservation = new FadSetObservation(
            new Coordinate(0.5, 0.5),
            new double[]{100, 100},
            123
        );
        final FadSetObservation secondObservation = new FadSetObservation(
            new Coordinate(0.5, 0.5),
            new double[]{10, 10},
            123
        );
        final HashMap<Integer, List<FadSetObservation>> dataset = new HashMap<>();
        dataset.put(123, Lists.newArrayList(firstObservation, secondObservation));
        final ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dataset);
        // let's log this
        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        final Species secondSpecies = new Species("Species 1");
        secondSpecies.resetIndexTo(1);
        when(model.getSpecies()).thenReturn(
            Lists.newArrayList(
                new Species("Species 0"),
                secondSpecies
            )
        );
        setter.startOrResetLogger(model);

        // 4 simulated fads in the water: holding 10,10;20,20;30,30;40,40 biomass
        final Bag localFads = new Bag();
        final AggregatingFad fad = mock(AggregatingFad.class, RETURNS_DEEP_STUBS);
        final LocalBiology biology = mock(LocalBiology.class);
        when(fad.getBiology()).thenReturn(biology);
        when(biology.getBiomass(any())).thenReturn(40d);
//        when(fad.getBiomass()).thenReturn(new double[]{40,40});
        localFads.add(fad);

        setter.start(model);
        when(model.getDay()).thenReturn(123);
        when(model.getFadMap().fadsAt(any())).thenReturn(localFads);
        setter.step(model);

        // there should have been 1 match, not out of bounds, and the error ought to be sqrt(2 (100-40)^2) +
        // the penalty for unmatching
        Assertions.assertEquals(1, setter.getCounter().getColumn("Matches"), 0.0001);
        Assertions.assertEquals(1, setter.getCounter().getColumn("Failed Matches"), 0.0001);
        Assertions.assertEquals(0, setter.getCounter().getColumn("Out of Bounds"), 0.0001);
        Assertions.assertEquals(
            Math.sqrt(2 * Math.pow(100 - 40, 2)) + ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR,
            setter.getCounter().getColumn("Error"),
            0.0001
        );

        System.out.println(setter.printLog());

        Assertions.assertEquals("day,x,y,result,error,Species 0,Species 0_simulated,Species 1,Species 1_simulated\n" +
            "123,0,0,MATCH," + Math.sqrt(2 * Math.pow(100 - 40, 2)) + ",100.0,40.0,100.0,40.0\n" +
            "123,0,0,FAILED,NaN\n", setter.printLog());

    }

    @SuppressWarnings("rawtypes")
    @Test
    public void outOfBoundsSetsAreAccountedSeparately() {
        // 2 observed sets: both will be "out of the map"
        final FadSetObservation firstObservation = new FadSetObservation(
            new Coordinate(0.5, 0.5),
            new double[]{100, 100},
            123
        );
        final FadSetObservation secondObservation = new FadSetObservation(
            new Coordinate(0.5, 0.5),
            new double[]{10, 10},
            123
        );
        final HashMap<Integer, List<FadSetObservation>> dataset = new HashMap<>();
        dataset.put(123, Lists.newArrayList(firstObservation, secondObservation));
        final ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dataset);

        // 4 simulated fads in the water: holding 10,10;20,20;30,30;40,40 biomass
        final Bag localFads = new Bag();
        final AggregatingFad fad = mock(AggregatingFad.class, RETURNS_DEEP_STUBS);
        when(fad.getBiomass()).thenReturn(new double[]{40, 40});
        localFads.add(fad);

        final FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        setter.start(model);
        when(model.getDay()).thenReturn(123);
        when(model.getFadMap().fadsAt(any())).thenReturn(localFads);
        // everything will be out of the map
        when(model.getMap().getSeaTile(any(Coordinate.class))).thenReturn(null);
        setter.step(model);

        // there should have been 0 matches, 2 out of bounds, and the error ought to be
        // the penalty for out of bounds twice
        Assertions.assertEquals(0, setter.getCounter().getColumn("Matches"), 0.0001);
        Assertions.assertEquals(0, setter.getCounter().getColumn("Failed Matches"), 0.0001);
        Assertions.assertEquals(2, setter.getCounter().getColumn("Out of Bounds"), 0.0001);
        Assertions.assertEquals(
            2 * ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR,
            setter.getCounter().getColumn("Error"),
            0.0001
        );
    }

    @Test
    public void fadsAreDroppedAndSetInActualScenario() {
        final FishState state = generateAndRunOneYearOfAbstractFadScenario(
            InputPath.of("inputs", "tests", "fad_dummy_sets.csv"),
            0,
            2
        );
        // should by now have beached or left the map
        Assertions.assertEquals(state.getFadMap().allFads().collect(Collectors.toList()).size(), 0);
        // there should have been 2 matches, 1 failed match (day 0) and 1 out of bounds
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Failed Matches"),
            1,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Matches"),
            2,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Out of Bounds"),
            1,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Error"),
            ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR + ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR +
                2 * Math.sqrt(2 * Math.pow(10, 2)),
            .001d
        );

    }

    private FishState generateAndRunOneYearOfAbstractFadScenario(
        final InputPath setterFile,
        final int neighborhoodSearchSize,
        final int expectedFadsRemainingAfter10Steps
    ) {
        final FlexibleScenario scenario = new FlexibleScenario();
        ((SimpleMapInitializerFactory) scenario.getMapInitializer()).setMaxLandWidth(new FixedDoubleParameter(1));
        ((SimpleMapInitializerFactory) scenario.getMapInitializer()).setCoastalRoughness(new FixedDoubleParameter(0));
        // two species
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        scenario.getFisherDefinitions().get(0).setInitialFishersPerPort(new LinkedHashMap<>());

        final FadDemoFactory fadDemo = new FadDemoFactory();
        fadDemo.setBiomassOnly(true);
        // assume a current that pushes you diagonally towards top-left
        fadDemo.setFixedXCurrent(new FixedDoubleParameter(+1));
        fadDemo.setFixedYCurrent(new FixedDoubleParameter(-1));
        fadDemo.setPathToFile(InputPath.of("inputs", "tests", "fad_dummy_deploy2.csv"));
        // they will all be empty!
        fadDemo.setFadInitializer(
            new CompressedBiomassFadInitializerFactory(
                new FixedDoubleParameter(445_000),
                "Species 0"
            )
        );
        scenario.getPlugins().add(fadDemo);

        // now add the fad setter
        final ExogenousFadSetterCSVFactory setters = new ExogenousFadSetterCSVFactory();
        setters.setNeighborhoodSearchSize(new FixedDoubleParameter(neighborhoodSearchSize));
        setters.setSetsFile(setterFile);
        setters.setDataInTonnes(true);
        scenario.getPlugins().add(setters);

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while (state.getDay() <= 10)
            state.schedule.step(state);
        // there ought to be 2 fads left (4 dropped, 2 landed!)
        Assertions.assertEquals(state.getFadMap().allFads().count(), expectedFadsRemainingAfter10Steps);

        // go to the end of the year
        while (state.getDay() <= 366)
            state.schedule.step(state);
        return state;
    }

    @Test
    public void fadsAreDroppedButMissedInActualScenario() {
        final FishState state = generateAndRunOneYearOfAbstractFadScenario(
            InputPath.of("inputs", "tests", "fad_dummy_sets2.csv"),
            0,
            4
        );
        // should by now have beached or left the map
        Assertions.assertEquals(state.getFadMap().allFads().collect(Collectors.toList()).size(), 0);
        // there should have been 0 matches, 3 failed matches  and 1 out of bounds
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Failed Matches"),
            3,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Matches"),
            0,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Out of Bounds"),
            1,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Error"),
            3 * ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR +
                ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR,
            .001d
        );

    }

    @Test
    public void fadsAreDroppedAndNotMissedBecauseOfNeighborhoodRangeInActualScenario() {
        final FishState state = generateAndRunOneYearOfAbstractFadScenario(
            InputPath.of("inputs", "tests", "fad_dummy_sets2.csv"),
            1,
            2
        );
        // should by now have beached or left the map
        Assertions.assertEquals(state.getFadMap().allFads().count(), 0);

        // there should have been 2 matches, 1 failed match (day 0) and 1 out of bounds
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Failed Matches"),
            1,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Matches"),
            2,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Out of Bounds"),
            1,
            .001d
        );
        Assertions.assertEquals(
            state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Error"),
            ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR + ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR +
                2 * Math.sqrt(2 * Math.pow(10, 2)),
            .001d
        );

    }

}
