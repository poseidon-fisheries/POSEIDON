package uk.ac.ox.oxfish.geography;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.mapmakers.MapInitializer;
import uk.ac.ox.oxfish.geography.mapmakers.MapWithFarOffPortsInitializerFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.factory.FixedPriceMarketFactory;
import uk.ac.ox.oxfish.model.scenario.FlexibleScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.yaml.FishYAML;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class NauticalMapWithFarOffPortsTest {



    @Test
    public void ditanceIsCorrect() {

        FishState state = setupSimpleOneFarOffPortState();


        //the port ought to exist
        final Predicate<Port> matchByName = port -> port.getName().equals("Ahoy");
        assertTrue(state.getPorts().stream().anyMatch(matchByName));
        assertEquals(state.getPorts().stream().filter(matchByName).count(),
        1);

        final Port ahoy = state.getPorts().stream().filter(matchByName).findFirst().get();


        double distanceFromExit = state.getMap().distance(
                ahoy.getLocation(),
                state.getMap().getSeaTile(0,0)
        );
        assertEquals(100,distanceFromExit,.001);

        double distanceFromCorner = state.getMap().distance(
                ahoy.getLocation(),
                state.getMap().getSeaTile(19,0)
        );
        //cross 19 more squares
        assertEquals(100+10*19,distanceFromCorner,.001);


    }


    @Test
    public void pathfindingIsCorrect() {

        FishState state = setupSimpleOneFarOffPortState();


        //the port ought to exist
        final Predicate<Port> matchByName = port -> port.getName().equals("Ahoy");
        assertTrue(state.getPorts().stream().anyMatch(matchByName));
        assertEquals(state.getPorts().stream().filter(matchByName).count(),
                1);

        final Port ahoy = state.getPorts().stream().filter(matchByName).findFirst().get();


        final SeaTile exitTile = state.getMap().getSeaTile(0, 0);
        final Deque<SeaTile> routeToExit = state.getMap().getRoute(
                ahoy.getLocation(),
                exitTile
        );
        assertEquals(2,routeToExit.size());
        assertEquals(ahoy.getLocation(),routeToExit.getFirst());
        assertEquals(exitTile,routeToExit.getLast());


        final Deque<SeaTile> routeToCorner = state.getMap().getRoute(
                ahoy.getLocation(),
                state.getMap().getSeaTile(18,0)
        );
        assertEquals(20,routeToCorner.size());
        assertEquals(ahoy.getLocation(),routeToCorner.removeFirst());
        assertEquals(exitTile,routeToCorner.removeFirst());
        for(int i=1;i<=18; i++)
            assertEquals(
                    state.getMap().getSeaTile(i,0)
                    ,routeToCorner.removeFirst());



    }


    @Test
    public void switchFromFarOffToNonFarOff() throws FileNotFoundException {

        //reading it from yaml because the PortInformation list looks weird
        FishYAML yaml = new FishYAML();
        final FlexibleScenario scenario = yaml.loadAs(
                new FileReader(
                        Paths.get("inputs", "tests", "faroff.yaml").toFile()),
                FlexibleScenario.class);

        final FarOffPortInformation information =
                ((MapWithFarOffPortsInitializerFactory) scenario.getMapInitializer()).
                        getFarOffPorts().get(0);

        //expensive gas!
        information.setGasPriceAtPort(100);
        //far away
        information.setDistanceFromExitInKm(50);

        scenario.setPortSwitching(true);

        FishState state = new FishState(1);
        state.setScenario(scenario);
        state.start();
        final Port farOffPort = ((NauticalMapWithFarOffPorts) state.getMap()).
                getAllFarOffPorts().iterator().next().getPort();
        //should be the far off port
        while(state.getYear()<10) {
            state.schedule.step(state);
            if(state.getDayOfTheYear()==1)
            {
                final long fishersFarOff = state.getFishers().stream().filter(
                        fisher -> fisher.getHomePort().equals(farOffPort)
                ).count();
                System.out.println(fishersFarOff);


            }
        }
        assertTrue("not enough people have left the port!",
                state.getFishers().stream().filter(
                fisher -> fisher.getHomePort().equals(farOffPort)
                ).count()< 3);





    }

    @Test
    public void switchToFarOffPortBecauseItPaysSoMuch() throws FileNotFoundException {

        //reading it from yaml because the PortInformation list looks weird
        FishYAML yaml = new FishYAML();
        final FlexibleScenario scenario = yaml.loadAs(
                new FileReader(
                        Paths.get("inputs", "tests", "faroff.yaml").toFile()),
                FlexibleScenario.class);

        final FarOffPortInformation information =
                ((MapWithFarOffPortsInitializerFactory) scenario.getMapInitializer()).
                        getFarOffPorts().get(0);

        //expensive gas!
        information.setGasPriceAtPort(0);
        final FixedPriceMarketFactory marketMaker = new FixedPriceMarketFactory();
        marketMaker.setMarketPrice(new FixedDoubleParameter(1000d));
        information.setMarketMaker(
                marketMaker
        );
        //far away
        information.setDistanceFromExitInKm(50);

        scenario.setPortSwitching(true);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        final Port farOffPort = ((NauticalMapWithFarOffPorts) state.getMap()).
                getAllFarOffPorts().iterator().next().getPort();
        //should be the far off port
        while(state.getYear()<10) {
            state.schedule.step(state);
            if(state.getDayOfTheYear()==1)
            {
                final long fishersFarOff = state.getFishers().stream().filter(
                        fisher -> fisher.getHomePort().equals(farOffPort)
                ).count();
                System.out.println(fishersFarOff);


            }
        }
        assertTrue("not enough people have left the port!",
                state.getFishers().stream().filter(
                        fisher -> fisher.getHomePort().equals(farOffPort)
                ).count() > 35);





    }


    @NotNull
    public FishState setupSimpleOneFarOffPortState() {
        PrototypeScenario scenario = new PrototypeScenario();

        final MapWithFarOffPortsInitializerFactory mapInitializer = new MapWithFarOffPortsInitializerFactory();
        final FarOffPortInformation singlePort = new FarOffPortInformation();
        singlePort.setDistanceFromExitInKm(100);
        singlePort.setExitGridX(0);
        singlePort.setExitGridY(0);
        singlePort.setPortName("Ahoy");
        singlePort.setGasPriceAtPort(0);

        mapInitializer.getFarOffPorts().add(
                singlePort
        );
        mapInitializer.setDelegate(new SimpleMapInitializerFactory(20,20,
                1,1,10));

        scenario.setMapInitializer(mapInitializer);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        return state;
    }
}