package uk.ac.ox.oxfish.model.scenario;

import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.initializer.factory.YellowBycatchFactory;
import uk.ac.ox.oxfish.fisher.equipment.gear.factory.FixedProportionGearFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.geography.ports.PortListFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Supplier;

/**
 * The Phil Levin two populations model. In reality just a facade on the usual two population scenarios
 * (with custom port - suppliers)
 * Created by carrknight on 3/13/17.
 */
public class SimpleCaliforniaScenario extends TwoPopulationsScenario {


    {
        YellowBycatchFactory biologyInitializer = new YellowBycatchFactory();
        biologyInitializer.setVerticalSeparator(new FixedDoubleParameter(5));
        this.setBiologyInitializer(biologyInitializer);

        //assuming all agents are just long-liners a la WFS
        this.setSmallHoldSize(new FixedDoubleParameter(6500));
        this.setLargeHoldSize(new FixedDoubleParameter(6500));
        this.setSmallSpeed(new FixedDoubleParameter(16.0661));
        this.setLargeSpeed(new FixedDoubleParameter(16.0661));
        //this is the original 1.21 gallons a mile in California we transformed in liters per km
        this.setSmallLitersPerKilometer(new FixedDoubleParameter(3.418019));
        this.setLargeLitersPerKilometer(new FixedDoubleParameter(3.418019));
        //infinite fuel size
        this.setSmallFuelTankSize(new FixedDoubleParameter(100000000));
        this.setLargeFuelTankSize(new FixedDoubleParameter(100000000));
        // 2.849 $/gallon to $/liter
        this.setGasPricePerLiter(new FixedDoubleParameter(0.626692129));

        //ratio width/height comes from the original california bathymetry
        //size of the cell is assuming max 120km distance to fish
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setHeight(new FixedDoubleParameter(100));
        mapInitializer.setWidth(new FixedDoubleParameter(11));
        mapInitializer.setMaxLandWidth(new FixedDoubleParameter(1));
        mapInitializer.setCellSizeInKilometers(new FixedDoubleParameter(1547d/100d)); //1547 km from los angeles to seattle
        this.setMapInitializer(mapInitializer);


        FixedProportionGearFactory gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(.001));
        this.setGearLarge(gear);
        this.setGearSmall(gear);


        PortListFactory ports = new PortListFactory();
        ports.getPorts().clear();
        ports.getPorts().put("Washington",new Coordinate(10,0));
        ports.getPorts().put("California",new Coordinate(10,99));
        this.setPorts(ports);

        this.setSmallFishers(50);
        this.setLargeFishers(50);


    }


    @Override
    protected Supplier<Port> getLargePortSupplier(
            MersenneTwisterFast random, Port[] ports) {
        //random
        return () -> ports[random.nextInt(ports.length)];
    }

    @Override
    protected Supplier<Port> getSmallPortSupplier(
            MersenneTwisterFast random, Port[] ports) {

        //always the first!
        return () -> ports[0];
    }
}
