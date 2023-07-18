package uk.ac.ox.oxfish.biology.growers;

import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.ArrayList;

public class CommonLogisticGrowerTest {


    @Test
    public void growsCorrectly() {

        PrototypeScenario scenario = new PrototypeScenario();
        scenario.setFishers(0);

        DiffusingLogisticFactory biology = new DiffusingLogisticFactory();
        scenario.setBiologyInitializer(biology);
        CommonLogisticGrowerFactory grower = new CommonLogisticGrowerFactory();
        biology.setGrower(grower);
        grower.setSteepness(new FixedDoubleParameter(.33));

        biology.setCarryingCapacity(new FixedDoubleParameter(5000));
        biology.setMaxInitialCapacity(new FixedDoubleParameter(.75));
        biology.setMinInitialCapacity(new FixedDoubleParameter(.75));

        SimpleMapInitializerFactory map = new SimpleMapInitializerFactory();
        scenario.setMapInitializer(map);
        map.setWidth(new FixedDoubleParameter(3));
        map.setHeight(new FixedDoubleParameter(1));
        map.setCoastalRoughness(new FixedDoubleParameter(0));
        map.setMaxLandWidth(new FixedDoubleParameter(1));

        FishState model = new FishState();
        model.setScenario(scenario);
        model.start();

        Assertions.assertEquals(7500, model.getTotalBiomass(model.getBiology().getSpecie(0)), .001);

        for (int i = 0; i < 370; i++)
            model.schedule.step(model);


        Assertions.assertEquals(8118.75, model.getTotalBiomass(model.getBiology().getSpecie(0)), .001);
    }

    @Test
    public void allocateEqually() {

        BiomassLocalBiology first = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        BiomassLocalBiology second = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        ArrayList<BiomassLocalBiology> biologies = Lists.newArrayList(first, second);

        CommonLogisticGrower.allocateBiomassProportionally(
            biologies,
            100,
            0,
            1
        );

        Assertions.assertEquals(150, first.getCurrentBiomass()[0], .0001);
        Assertions.assertEquals(150, second.getCurrentBiomass()[0], .0001);


    }

    @Test
    public void fillToBrim() {

        BiomassLocalBiology first = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        BiomassLocalBiology second = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        ArrayList<BiomassLocalBiology> biologies = Lists.newArrayList(first, second);

        CommonLogisticGrower.allocateBiomassProportionally(
            biologies,
            1000,
            0,
            1
        );

        Assertions.assertEquals(200, first.getCurrentBiomass()[0], .0001);
        Assertions.assertEquals(200, second.getCurrentBiomass()[0], .0001);


    }


    @Test
    public void alreadyFull() {

        BiomassLocalBiology first = new BiomassLocalBiology(new double[]{200d}, new double[]{200d});
        BiomassLocalBiology second = new BiomassLocalBiology(new double[]{200d}, new double[]{200d});
        ArrayList<BiomassLocalBiology> biologies = Lists.newArrayList(first, second);

        CommonLogisticGrower.allocateBiomassProportionally(
            biologies,
            1000,
            0,
            1
        );

        Assertions.assertEquals(200, first.getCurrentBiomass()[0], .0001);
        Assertions.assertEquals(200, second.getCurrentBiomass()[0], .0001);


    }

    @Test
    public void allocateMoreToEmptyOne() {

        BiomassLocalBiology first = new BiomassLocalBiology(new double[]{0d}, new double[]{200d});
        BiomassLocalBiology second = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        ArrayList<BiomassLocalBiology> biologies = Lists.newArrayList(first, second);

        CommonLogisticGrower.allocateBiomassProportionally(
            biologies,
            100,
            0,
            1
        );

        Assertions.assertEquals(100 * (200d / 300), first.getCurrentBiomass()[0], .0001);
        Assertions.assertEquals(100 + 100d * (100d / 300), second.getCurrentBiomass()[0], .0001);


    }

    @Test
    public void allocateMoreToEmptyOneAgain() {

        BiomassLocalBiology first = new BiomassLocalBiology(new double[]{0d}, new double[]{200d});
        BiomassLocalBiology second = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        ArrayList<BiomassLocalBiology> biologies = Lists.newArrayList(first, second);

        CommonLogisticGrower.allocateBiomassProportionally(
            biologies,
            250,
            0,
            1
        );

        Assertions.assertEquals(250d * (200d / 300), first.getCurrentBiomass()[0], .0001);
        Assertions.assertEquals(100 + 250d * (100d / 300), second.getCurrentBiomass()[0], .0001);


    }

    @Test
    public void allocateMoreToEmptyOneWithWeight() {

        BiomassLocalBiology first = new BiomassLocalBiology(new double[]{0d}, new double[]{200d});
        BiomassLocalBiology second = new BiomassLocalBiology(new double[]{100d}, new double[]{200d});
        ArrayList<BiomassLocalBiology> biologies = Lists.newArrayList(first, second);

        CommonLogisticGrower.allocateBiomassProportionally(
            biologies,
            250,
            0,
            3
        );

        //fill this up first!
        Assertions.assertEquals(200, first.getCurrentBiomass()[0], .0001);
        Assertions.assertEquals(150, second.getCurrentBiomass()[0], .0001);


    }
}