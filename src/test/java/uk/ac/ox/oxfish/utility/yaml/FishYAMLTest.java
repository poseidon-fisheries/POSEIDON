package uk.ac.ox.oxfish.utility.yaml;

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.model.regs.FishingSeason;
import uk.ac.ox.oxfish.model.regs.ProtectedAreasOnly;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.FishingSeasonFactory;
import uk.ac.ox.oxfish.model.regs.factory.ProtectedAreasOnlyFactory;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;
import uk.ac.ox.oxfish.model.scenario.CaliforniaBathymetryScenario;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import static org.junit.Assert.*;


public class FishYAMLTest {


    @Test
    public void canReadAScenario() throws Exception
    {

        String scenarioFile = "!!uk.ac.ox.oxfish.model.scenario.PrototypeScenario\n" +
                //here i am just using the constructor name as in the CONSTRUCTOR_MAP and then a simple map
                "biologyInitializer:\n" +
                "  Diffusing Logistic:\n"+
                "    carryingCapacity: 14.0\n" +
                "    differentialPercentageToMove: 5.0E-4\n" +
                "    maxSteepness: 0.8\n" +
                "    minSteepness: 0.7\n" +
                "    percentageLimitOnDailyMovement: uniform '0.001 0.01'\n" +
                "coastalRoughness: 4\n" +
                "departingStrategy:\n" +
                "  Fixed Probability:\n" +
                "    probabilityToLeavePort: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "      fixedValue: 1.0\n" +
                "depthSmoothing: 1000000\n" +
                "destinationStrategy: !!uk.ac.ox.oxfish.fisher.strategies.destination.factory.PerTripIterativeDestinationFactory\n" +
                "  stepSize: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "    fixedValue: 5.0\n" +
                "  tripsPerDecision: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "    fixedValue: 1.0\n" +
                "fishers: 1234\n" +
                "fishingEfficiency: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "  fixedValue: 0.01\n" +
                "fishingStrategy: !!uk.ac.ox.oxfish.fisher.strategies.fishing.factory.MaximumStepsFactory\n" +
                "  daysAtSea: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "    fixedValue: 10.0\n" +
                "gridSizeInKm: 10.0\n" +
                "height: 50\n" +
                "holdSize: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "  fixedValue: 100.0\n" +
                "networkBuilder: !!uk.ac.ox.oxfish.model.network.EquidegreeBuilder\n" +
                "  degree: 2\n" +
                "ports: 1\n" +
                //here i am calling the regulation object by !!
                "regulation:\n" +
                "  Anarchy\n" +
                "speedInKmh: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
                "  fixedValue: 5.0\n" +
                "width: 50";

        FishYAML yaml = new FishYAML();
        final Object loaded = yaml.load(scenarioFile);
        //read prototype scenario correctly
        Assert.assertTrue(loaded instanceof PrototypeScenario);
        PrototypeScenario scenario = (PrototypeScenario) loaded;
        //read initializer correctly
        Assert.assertTrue(scenario.getBiologyInitializer() instanceof DiffusingLogisticFactory);
        DiffusingLogisticFactory factory = (DiffusingLogisticFactory) scenario.getBiologyInitializer();
        //reads double parameters correctly
        Assert.assertTrue(factory.getCarryingCapacity() instanceof FixedDoubleParameter);
        Assert.assertEquals(((FixedDoubleParameter) factory.getCarryingCapacity()).getFixedValue(),14.0,.001);
        //reads normal doubles correctly
        Assert.assertEquals(((FixedDoubleParameter) factory.getMinSteepness()).getFixedValue(), .7, .0001);
        //reads anarchy factory just as well (it's a scalar algorithmFactory which is tricky)
        Assert.assertTrue(scenario.getRegulation() instanceof AnarchyFactory);

    }


    @Test
    public void writePrettilyAndReadBack() throws Exception {

        DiffusingLogisticFactory factory = new DiffusingLogisticFactory();
        factory.setCarryingCapacity(new NormalDoubleParameter(10000, 10));
        factory.setMaxSteepness(new UniformDoubleParameter(0, 10));
        factory.setDifferentialPercentageToMove(new FixedDoubleParameter(.001));
        FishYAML yaml = new FishYAML();
        final String dumped = yaml.dump(factory);
        System.out.println(dumped);

        //test pretty printing
        Assert.assertTrue(dumped.contains("maxSteepness: uniform 0.0 10.0"));
        Assert.assertTrue(dumped.contains("carryingCapacity: normal 10000.0 10.0"));

        //now read it back! (notice that I need to do "loadAs" because when writing prettily the factory gets written
        //as a map; that's not an issue in scenarios because the constructor knows where factories ought to be but when
        //the factory is written without any warning that it's going to be an AlgorithmFactory then things go badly
        DiffusingLogisticFactory factory2 = yaml.loadAs(dumped, DiffusingLogisticFactory.class);
        assertEquals(((NormalDoubleParameter) factory2.getCarryingCapacity()).getMean(), 10000, .001);
        assertEquals(((NormalDoubleParameter) factory2.getCarryingCapacity()).getStandardDeviation(), 10, .001);

    }

    @Test
    public void writePrettilyAllSortsOfScenarios()
    {
        PrototypeScenario scenario = new PrototypeScenario();
        ((DiffusingLogisticFactory) scenario.getBiologyInitializer()).setMaxSteepness(new FixedDoubleParameter(.9));
        scenario.setRegulation(Regulations.CONSTRUCTORS.get("MPA Only").get());
        FishYAML yaml = new FishYAML();
        final String dumped = yaml.dump(scenario);
        //load back! Notice that because it's made "pretty" I still have to call loadAs
        Scenario scenario2 = (Scenario) yaml.loadAs(dumped, Scenario.class);
        Assert.assertTrue(scenario2 instanceof PrototypeScenario);

        //make sure it remembers that the regulations have changed
        Assert.assertTrue(((PrototypeScenario) scenario2).getRegulation() instanceof ProtectedAreasOnlyFactory);
        //make sure three recursions in this is still correct.
        Assert.assertEquals(
                ((FixedDoubleParameter) ((DiffusingLogisticFactory) ((PrototypeScenario) scenario2).getBiologyInitializer()).getMaxSteepness()).getFixedValue(),.9,.0001);


        //final test, if I redump you, it'll be exactly like before
        String dump2 = yaml.dump(scenario2);
        Assert.assertEquals(dumped,dump2);
    }
}