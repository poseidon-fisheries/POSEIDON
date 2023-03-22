package uk.ac.ox.oxfish.geography.fads;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Test;
import sim.util.Double2D;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.LocalBiology;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.AbstractFad;
import uk.ac.ox.oxfish.fisher.purseseiner.fads.BiomassFad;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.InputPath;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.ToDoubleFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ExogenousFadMakerTest {


    @Test
    public void dropsFadsCorrectly() {

        final FishState state = new FishState();

        //create simple scenario with no boats
        final FlexibleScenario scenario = new FlexibleScenario();
        final SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        //5x5 map
        mapInitializer.setCoastalRoughness(new FixedDoubleParameter(0));
        mapInitializer.setHeight(new FixedDoubleParameter(5));
        mapInitializer.setWidth(new FixedDoubleParameter(5));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        scenario.setMapInitializer(mapInitializer);
        //no fishers!
        scenario.getFisherDefinitions().get(0).setInitialFishersPerPort(new LinkedHashMap<>());


        state.setScenario(scenario);
        state.start();
        //add a simple FADMap with fixed currents
        final FadMapDummyFactory factory = new FadMapDummyFactory();
        factory.setBiomassOnly(true);
        final FadMap actualMap = factory.apply(state);
        state.registerStartable(actualMap);
        assertTrue(actualMap.isStarted()); //make sure the FADMap has started
        //and now let's add the exogenous fad maker: it is going to try and put a fad at day 3!
        final FadInitializer<BiomassLocalBiology, BiomassFad> fakeFadInitializer = mock(FadInitializer.class);
        //mason primitives are accessed by "setlocation"
        //which means that returning the same mock twice will result in the first being moved, rather than double deployment
        when(fakeFadInitializer.makeFad(any(), any(), any())).thenReturn(
            mock(BiomassFad.class),
            mock(BiomassFad.class)
        );
        final HashMap<Integer, Collection<Double2D>> dayToCoordinatesMap = new HashMap<>();
        dayToCoordinatesMap.put(
            3,
            Lists.newArrayList(
                new Double2D(0.5, 0.5),
                new Double2D(0.3, 0.6)
            )

        ); //only two fads, at seatile 0,4
        final ExogenousFadMaker<BiomassLocalBiology, BiomassFad> fadMaker = new ExogenousFadMaker<>(
            fakeFadInitializer,
            dayToCoordinatesMap
        );
        state.registerStartable(fadMaker);
        state.schedule.step(state); //day 0
        state.schedule.step(state);
        state.schedule.step(state);
        verify(fakeFadInitializer, never()).makeFad(any(), any(), any());
        state.schedule.step(state);
        //you should have deployed 2 fads, both at 0,4
        verify(fakeFadInitializer, times(2)).makeFad(any(), any(), any());
        assertEquals(state.getFadMap().fadsAt(state.getMap().getSeaTile(0, 4)).size(), 2);
        assertEquals(state.getFadMap().getDriftingObjectsMap().getField().allObjects.size(), 2);
        assertEquals(state.getFadMap().fadsAt(state.getMap().getSeaTile(1, 1)).size(), 0);
    }

    @Test
    public void fadsAreDroppedInActualScenario() {
        final FlexibleScenario scenario = new FlexibleScenario();
        scenario.getFisherDefinitions().get(0).setInitialFishersPerPort(new LinkedHashMap<>());

        final FadDemoFactory fadDemo = new FadDemoFactory();
        fadDemo.setBiomassOnly(true);
        //assume a current that pushes you diagonally towards top-left
        fadDemo.setFixedXCurrent(new FixedDoubleParameter(+1));
        fadDemo.setFixedYCurrent(new FixedDoubleParameter(-1));
        fadDemo.setPathToFile(InputPath.of("inputs", "tests", "fad_dummy_deploy.csv"));
        ((BiomassFadInitializerFactory) fadDemo.getFadInitializer()).setGrowthRates(
            ImmutableMap.of(
                "Species 0",
                new FixedDoubleParameter(0.1)
            )
        );
        scenario.getPlugins().add(fadDemo);

        final FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        while (state.getDay() <= 13)
            state.schedule.step(state);
        System.out.println(state.getDay());
        //there ought to be 12 fads at play
        Assert.assertEquals(
            state.getFadMap().allFads().count(),
            12
        );
        //the first we dropped was at 5,5 coordinates, which translates to 5,44 in seatile grids (right there at the edge, really)
        //it moved left and up for 13 steps (from day 2 to 14, considering it steps the very same day it is dropped)
        // so it should be at 18,
        assertEquals(1, state.getFadMap().fadsAt(state.getMap().getSeaTile(18, 31)).size());
        //there should be at least one FAD out there that has attracted some biomass (probability of attraction is 63% per step per FAD)
        assertTrue(state.getFadMap()
            .allFads()
            .mapToDouble((ToDoubleFunction<AbstractFad<? extends LocalBiology, ? extends AbstractFad<?, ?>>>) value -> value.getBiology()
                .getBiomass(state.getSpecies("Species 0")))
            .sum() > 0);


    }
}