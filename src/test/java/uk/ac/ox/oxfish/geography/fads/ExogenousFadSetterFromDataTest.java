package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;
import sim.util.Bag;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.Fad;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ExogenousFadSetterFromDataTest {


    @Test
    public void choosesTheCorrectFADs() {


        //2 observed sets: the first landing 100,100 the second landing 10,10
        FadSetObservation firstObservation = new FadSetObservation(new Coordinate(0.5,0.5),new double[]{100,100},123);
        FadSetObservation secondObservation = new FadSetObservation(new Coordinate(0.5,0.5),new double[]{10,10},123);
        HashMap<Integer, List<FadSetObservation>> dataset = new HashMap<>();
        dataset.put(123,Lists.newArrayList(firstObservation,secondObservation));
        ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dataset);


        //4 simulated fads in the water: holding 10,10;20,20;30,30;40,40 biomass
        Bag localFads = new Bag();
        for(int i=1;i<=4;i++) {
            Fad fad = mock(Fad.class,RETURNS_DEEP_STUBS);
            when(fad.getBiomass()).thenReturn(new double[]{i*10,i*10});
            localFads.add(fad);
        }

        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        setter.start(model);
        when(model.getDay()).thenReturn(123);
        when(model.getFadMap().fadsAt(any())).thenReturn(localFads);
        setter.step(model);

        //there should have been 2 matches, not out of bounds, and the error ought to be sqrt(2(100-40)^2) since second
        //fad has perfect match!
        assertEquals(2,setter.getCounter().getColumn("Matches"),0.0001);
        assertEquals(0,setter.getCounter().getColumn("Failed Matches"),0.0001);
        assertEquals(0,setter.getCounter().getColumn("Out of Bounds"),0.0001);
        assertEquals(
                Math.sqrt(2*Math.pow(100-40,2))
                ,setter.getCounter().getColumn("Error"),0.0001);

    }


    @Test
    public void twoObservationsOneFad() {

        //2 observed sets: the first landing 100,100 the second landing 10,10
        FadSetObservation firstObservation = new FadSetObservation(new Coordinate(0.5,0.5),new double[]{100,100},123);
        FadSetObservation secondObservation = new FadSetObservation(new Coordinate(0.5,0.5),new double[]{10,10},123);
        HashMap<Integer, List<FadSetObservation>> dataset = new HashMap<>();
        dataset.put(123,Lists.newArrayList(firstObservation,secondObservation));
        ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dataset);
        //let's log this
        setter.startOrResetLogger(mock(FishState.class));


        //4 simulated fads in the water: holding 10,10;20,20;30,30;40,40 biomass
        Bag localFads = new Bag();
        Fad fad = mock(Fad.class,RETURNS_DEEP_STUBS);
        when(fad.getBiomass()).thenReturn(new double[]{40,40});
        localFads.add(fad);


        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        setter.start(model);
        when(model.getDay()).thenReturn(123);
        when(model.getFadMap().fadsAt(any())).thenReturn(localFads);
        setter.step(model);

        //there should have been 1 match, not out of bounds, and the error ought to be sqrt(2 (100-40)^2) +
        // the penalty for unmatching
        assertEquals(1,setter.getCounter().getColumn("Matches"),0.0001);
        assertEquals(1,setter.getCounter().getColumn("Failed Matches"),0.0001);
        assertEquals(0,setter.getCounter().getColumn("Out of Bounds"),0.0001);
        assertEquals(
                Math.sqrt(2* Math.pow(100-40,2)) + ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR
                ,setter.getCounter().getColumn("Error"),0.0001);

        System.out.println(setter.printLog());

        assertEquals("day,x,y,result,error\n" +
                "123,0,0,MATCH,"+ Math.sqrt(2 * Math.pow(100 - 40, 2)) +"\n" +
                "123,0,0,FAILED,NaN" +"\n",
                setter.printLog()

                );

    }

    @Test
    public void outOfBoundsSetsAreAccountedSeparately() {
        //2 observed sets: both will be "out of the map"
        FadSetObservation firstObservation = new FadSetObservation(new Coordinate(0.5,0.5),new double[]{100,100},123);
        FadSetObservation secondObservation = new FadSetObservation(new Coordinate(0.5,0.5),new double[]{10,10},123);
        HashMap<Integer, List<FadSetObservation>> dataset = new HashMap<>();
        dataset.put(123,Lists.newArrayList(firstObservation,secondObservation));
        ExogenousFadSetterFromData setter = new ExogenousFadSetterFromData(dataset);


        //4 simulated fads in the water: holding 10,10;20,20;30,30;40,40 biomass
        Bag localFads = new Bag();
        Fad fad = mock(Fad.class,RETURNS_DEEP_STUBS);
        when(fad.getBiomass()).thenReturn(new double[]{40,40});
        localFads.add(fad);


        FishState model = mock(FishState.class, RETURNS_DEEP_STUBS);
        setter.start(model);
        when(model.getDay()).thenReturn(123);
        when(model.getFadMap().fadsAt(any())).thenReturn(localFads);
        //everything will be out of the map
        when(model.getMap().getSeaTile(any(Coordinate.class))).thenReturn(null);
        setter.step(model);

        //there should have been 0 matches, 2 out of bounds, and the error ought to be
        // the penalty for out of bounds twice
        assertEquals(0,setter.getCounter().getColumn("Matches"),0.0001);
        assertEquals(0,setter.getCounter().getColumn("Failed Matches"),0.0001);
        assertEquals(2 ,setter.getCounter().getColumn("Out of Bounds"),0.0001);
        assertEquals(
                2 *  ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR
                ,setter.getCounter().getColumn("Error"),0.0001);
    }

    @Test
    public void fadsAreDroppedAndSetInActualScenario() {
        FishState state = generateAndRunOneYearOfAbstractFadScenario("./inputs/tests/fad_dummy_sets.csv",
                0,
                2);
        //should by now have beached or left the map
        Assert.assertEquals(
                state.getFadMap().allFads().collect(Collectors.toList()).size(),
                0);
        //there should have been 2 matches, 1 failed match (day 0) and 1 out of bounds
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Failed Matches"),
                1,
                .001d
        );
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Matches"),
                2,
                .001d
        );
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Out of Bounds"),
                1,
                .001d
        );
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Error"),
                //1 missing, 1 out of bounds, and twice they should have hit empty
                ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR + ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR +
                2 * Math.sqrt(2*Math.pow(10,2)),
                .001d
        );

    }


    @Test
    public void fadsAreDroppedButMissedInActualScenario() {
        FishState state = generateAndRunOneYearOfAbstractFadScenario("./inputs/tests/fad_dummy_sets2.csv", 0, 4);
        //should by now have beached or left the map
        Assert.assertEquals(
                state.getFadMap().allFads().collect(Collectors.toList()).size(),
                0);
        //there should have been 0 matches, 3 failed matches  and 1 out of bounds
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Failed Matches"),
                3,
                .001d
        );
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Matches"),
                0,
                .001d
        );
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Out of Bounds"),
                1,
                .001d
        );
        assertEquals(
                state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Error"),
                //1 missing, 1 out of bounds, and twice they should have hit empty
                3* ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR + ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR,
                .001d
        );



    }

    @Test
    public void fadsAreDroppedAndNotMissedBecauseOfNeighborhoodRangeInActualScenario() {
            FishState state = generateAndRunOneYearOfAbstractFadScenario("./inputs/tests/fad_dummy_sets2.csv", 1, 2);
            //should by now have beached or left the map
            Assert.assertEquals(
                    state.getFadMap().allFads().collect(Collectors.toList()).size(),
                    0);


            //there should have been 2 matches, 1 failed match (day 0) and 1 out of bounds
            assertEquals(
                    state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Failed Matches"),
                    1,
                    .001d
            );
            assertEquals(
                    state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Matches"),
                    2,
                    .001d
            );
            assertEquals(
                    state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Out of Bounds"),
                    1,
                    .001d
            );
            assertEquals(
                    state.getYearlyDataSet().getLatestObservation("Exogenous Fad Setter Error"),
                    //1 missing, 1 out of bounds, and twice they should have hit empty
                    ExogenousFadSetterFromData.DEFAULT_MISSING_FAD_ERROR + ExogenousFadSetterFromData.OUT_OF_BOUNDS_FAD_ERROR +
                            2 * Math.sqrt(2 * Math.pow(10, 2)),
                    .001d
            );

    }

    @NotNull
    private FishState generateAndRunOneYearOfAbstractFadScenario(String setterFile,
                                                                 int neighborhoodSearchSize,
                                                                 int expectedFadsRemainingAfter10Steps) {
        FlexibleScenario scenario = new FlexibleScenario();
        ((SimpleMapInitializerFactory) scenario.getMapInitializer()).setMaxLandWidth(new FixedDoubleParameter(1));
        ((SimpleMapInitializerFactory) scenario.getMapInitializer()).setCoastalRoughness(new FixedDoubleParameter(0));
        //two species
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        scenario.getFisherDefinitions().get(0).setInitialFishersPerPort(new LinkedHashMap<>());

        FadDemoFactory fadDemo = new FadDemoFactory();
        fadDemo.setBiomassOnly(true);
        //assume a current that pushes you diagonally towards top-left
        fadDemo.setFixedXCurrent(new FixedDoubleParameter(+1));
        fadDemo.setFixedYCurrent(new FixedDoubleParameter(-1));
        fadDemo.setPathToFile("./inputs/tests/fad_dummy_deploy2.csv");
        //they will all be empty!
        ((BiomassFadInitializerFactory) fadDemo.getFadInitializer()).getGrowthRates().put("Species 0",
                new FixedDoubleParameter(0));
        scenario.getPlugins().add(fadDemo);

        //now add the fad setter
        ExogenousFadSetterCSVFactory setters = new ExogenousFadSetterCSVFactory();
        setters.setNeighborhoodSearchSize(new FixedDoubleParameter(neighborhoodSearchSize));
        setters.setPathToFile(setterFile);
        setters.setDataInTonnes(true);
        scenario.getPlugins().add(setters);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while(state.getDay()<=10)
            state.schedule.step(state);
        //there ought to be 2 fads left (4 dropped, 2 landed!)
        Assert.assertEquals(
                state.getFadMap().allFads().collect(Collectors.toList()).size(),
                expectedFadsRemainingAfter10Steps);

        //go to the end of the year
        while(state.getDay()<=366)
            state.schedule.step(state);
        return state;
    }


}