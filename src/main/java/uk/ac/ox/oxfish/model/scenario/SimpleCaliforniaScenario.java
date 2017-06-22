package uk.ac.ox.oxfish.model.scenario;

import com.google.common.collect.Lists;
import com.vividsolutions.jts.geom.Coordinate;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.initializer.factory.YellowBycatchWithHistoryFactory;
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
        YellowBycatchWithHistoryFactory biologyInitializer = new YellowBycatchWithHistoryFactory();

        //2001 start, if needed!
        biologyInitializer.setHistoricalTargetBiomass(Lists.newArrayList(
                338562.722077059 * 1000,
                338705.324234203 * 1000,
                342472.859205429* 1000,
                343751.711943503* 1000,
                345160.671539353* 1000
        ));
        biologyInitializer.setHistoricalTargetSurvival(Lists.newArrayList(0.904787532616812,0.906397451712637));
        /*
        "65" 1980 5884.75390821775 7570.39464071883 80.6977521701088 0.911643504584384 267.4
        "66" 1981 5684.81400583123 7539.11477741686 80.5797120779755 0.893149367230115 368.4
        "67" 1982 5388.82436349019 7501.04165097297 80.4351798236472 0.872913453252916 463.4
        "68" 1983 5003.39439583719 7461.07666659014 80.2824444569843 0.891801980573978 331.3
        "69" 1984 4755.7256703365 7354.80311652805 79.871124821994 0.899453305071633 276.8
        "70" 1985 4568.16526592639 7287.65567320182 79.6072914957053 0.886236958581081 329.1
        "71" 1986 4333.32840621945 7192.95479081678 79.2298782779113 0.906751653402573 219.1
        "72" 1987 4213.46491242035 7068.73430289459 78.7251436373954 0.898079385164327 251.3
        "73" 1988 4065.55744324838 6952.29316016972 78.2417453197403 0.889382661805629 279.5
        "74" 1989 3892.9123125654 6819.62353737464 77.678446943645 0.869935751715077 346.9
        "75" 1990 3655.63133792404 6699.58495536759 77.1568870268711 0.8930188400302 237.4
        "76" 1991 3531.3892303894 6555.81482839977 76.5167956590274 0.855516923649324 368
        "77" 1992 3278.22685605126 6409.65515493355 75.8481677369505 0.846432838984651 372.8
        "78" 1993 3021.32461787125 6144.85255271959 74.5883473973764 0.854299005073592 318.7
        "79" 1994 2820.02768880825 5884.75390821775 73.2860592436526 0.879382609525879 223.4
        "80" 1995 2716.72810126806 5684.81400583123 72.2380652253549 0.86553778449065 254.6
        "81" 1996 2582.87302878858 5388.82436349019 70.6058058792285 0.874543511909947 217.7
        "82" 1997 2485.78587079967 5003.39439583719 68.3217713609866 0.861256675081106 244.1
        "83" 1998 2361.38936282188 4755.7256703365 66.749550624956 0.911764934223519 107
        "84" 1999 2376.07942409806 4568.16526592639 65.4993369992089 0.892417837419521 155.8
        "85" 2000 2341.24361654434 4333.32840621945 63.8558847024621 0.938356073678119 40.9
        "86" 2001 2423.36370656817 4213.46491242035 62.9813932752828 0.932931166788219 56.1
         */
        biologyInitializer.setHistoricalBycatchBiomass(Lists.newArrayList(
                5884.75390821775* 1000,
                5684.81400583123* 1000,
                5388.82436349019* 1000,
                5003.39439583719* 1000,
                4755.7256703365* 1000,
                4568.16526592639* 1000,
                4333.32840621945* 1000,
                4213.46491242035* 1000,
                4065.55744324838* 1000,
                3892.9123125654* 1000,
                3655.63133792404* 1000,
                3531.3892303894* 1000,
                3278.22685605126* 1000,
                3021.32461787125* 1000,
                2820.02768880825* 1000,
                2716.72810126806* 1000,
                2582.87302878858* 1000,
                2485.78587079967* 1000,
                2361.38936282188* 1000,
                2376.07942409806* 1000,
                2341.24361654434* 1000,
                2423.36370656817* 1000

        ));
        biologyInitializer.setHistoricalTargetSurvival(Lists.newArrayList(
                0.938356073678119,
                0.932931166788219
        ));

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
        gear = new FixedProportionGearFactory();
        gear.setCatchabilityPerHour(new FixedDoubleParameter(.001));
        this.setGearSmall(gear);


        PortListFactory ports = new PortListFactory();
        ports.getPorts().clear();
        ports.getPorts().put("Washington",new Coordinate(10,0));
        ports.getPorts().put("Oregon",new Coordinate(10,49));
        ports.getPorts().put("California",new Coordinate(10,99));
        this.setPorts(ports);

        this.setSmallFishers(50);
        this.setLargeFishers(50);


    }


    @Override
    protected Supplier<Port> getLargePortSupplier(
            MersenneTwisterFast random, Port[] ports) {
        //random
        return () -> ports[random.nextInt(ports.length-1)+1];
    }

    @Override
    protected Supplier<Port> getSmallPortSupplier(
            MersenneTwisterFast random, Port[] ports) {

        //always the first!
        return () -> ports[0];
    }
}
