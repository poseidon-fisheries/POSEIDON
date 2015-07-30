package spikes;


import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.io.FileNotFoundException;

public class YamlSpike {

    @Test
    public void writeAndReadYaml() throws FileNotFoundException {

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer();
        Yaml yaml = new Yaml(representer,options);


        PrototypeScenario one = new PrototypeScenario();
        one.setFishers(1234);
        final DiffusingLogisticFactory biologyInitializer = new DiffusingLogisticFactory();
        biologyInitializer.setCarryingCapacity(new NormalDoubleParameter(10,.1));
        one.setBiologyInitializer(biologyInitializer);

        final String dumped = yaml.dump(one);
        System.out.println(dumped);
        PrototypeScenario two = (PrototypeScenario) yaml.load(dumped);


        Assert.assertEquals(two.getFishers(), one.getFishers());
        final AlgorithmFactory<? extends BiologyInitializer> newInitializer = two.getBiologyInitializer();
        Assert.assertTrue(newInitializer instanceof DiffusingLogisticFactory);
        final DoubleParameter carryingCapacity = ((DiffusingLogisticFactory) newInitializer).getCarryingCapacity();
        Assert.assertTrue(carryingCapacity instanceof  NormalDoubleParameter);
        Assert.assertEquals(((NormalDoubleParameter)carryingCapacity).getMean(),10,.0001);
        Assert.assertEquals(((NormalDoubleParameter) carryingCapacity).getStandardDeviation(),.1,.0001);

    }


    /**
     * needed to customize the way stuff is dumped
     */
    class FixedValueRepresenter extends Representer
    {

        public  FixedValueRepresenter()
        {
            this.representers.put(FixedDoubleParameter.class,new RepresentFixedValue());
        }


        private class RepresentFixedValue implements Represent {

            /**
             * Create a Node
             *
             * @param data the instance to represent
             * @return Node to dump
             */
            @Override
            public Node representData(Object data) {
                FixedDoubleParameter representer = (FixedDoubleParameter) data;
                String value =Double.toString(representer.getFixedValue());
                return representScalar(new Tag("!fixed"),value);
            }
        }
    }



    class FixedValueCostructor extends Constructor {

        public FixedValueCostructor()
        {
            final ConstructFixedValue constructor = new ConstructFixedValue();
            this.yamlConstructors.put(new Tag("!fixed"), constructor);
            this.yamlClassConstructors.put(NodeId.scalar,new ConstructorScalar() );
            this.yamlClassConstructors.put(NodeId.mapping, new ConstructFactory());
       //     this.yamlClassConstructors.put(NodeId.)
        }

        private class ConstructorScalar extends Constructor.ConstructScalar
        {
            @Override
            public Object construct(Node nnode) {
                System.out.println("scalar: " + nnode.getType());
                if(nnode.getType().equals(DoubleParameter.class))
                    return new FixedDoubleParameter((Double.parseDouble((String) constructScalar((ScalarNode) nnode))));
                else
                //this might be an algorithm-factory with no beans
                    if(AlgorithmFactory.class.isAssignableFrom(nnode.getType()))
                        return AlgorithmFactories.constructorLookup((String) constructScalar((ScalarNode) nnode));
                else
                    return super.construct(nnode);
            }
        }

        private class ConstructFixedValue implements Construct {
            /**
             * Construct a Java instance with all the properties injected when it is
             * possible.
             *
             * @param node composed Node
             * @return a complete Java instance
             */
            @Override
            public Object construct(Node node) {
                String val = (String) constructScalar((ScalarNode) node);
                return new FixedDoubleParameter(Double.parseDouble(val));
            }



            /**
             * Apply the second step when constructing recursive structures. Because the
             * instance is already created it can assign a reference to itself.
             *
             * @param node   composed Node
             * @param object the instance constructed earlier by
             */
            @Override
            public void construct2ndStep(Node node, Object object) {
            }
        }

        private class ConstructFactory extends  Constructor.ConstructMapping
        {

            @Override
            public Object construct(Node node) {
                System.out.println(node.getType());
                if(AlgorithmFactory.class.isAssignableFrom(node.getType())) {
                    //try super constructor first, tag might have been supplied!
                    try {
                        return super.construct(node);
                    } catch (YAMLException e) {
                        //the original construct failed, try just looking up the name

                        //hopefully it is written as a cogent map we can modify
                        final AlgorithmFactory toReturn = AlgorithmFactories.constructorLookup(
                                ((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue());
                        ((MappingNode) node).setValue(
                                ((MappingNode)((MappingNode) node).getValue().get(0).getValueNode()).getValue());
                        //todo might have to flatten here!
                        assert toReturn != null;
                        node.setType(toReturn.getClass());
                       constructJavaBean2ndStep((MappingNode) node, toReturn);
                        return toReturn;
                    }
                }
                else
                    return super.construct(node);
            }
        }
    }


    @Test
    public void implicitFixedValue() throws Exception {

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new FixedValueCostructor(),new Representer(),options);
        FixedDoubleParameter fixed = new FixedDoubleParameter(123);
        String dump = yaml.dump(fixed);
        System.out.println(dump);
        final FixedDoubleParameter read = (FixedDoubleParameter) yaml.load(dump);
        Assert.assertEquals(read.getFixedValue(), 123, .0001);

        //works if forced
        final FixedDoubleParameter read2 = (FixedDoubleParameter) yaml.loadAs("123.0", DoubleParameter.class);
        Assert.assertEquals(read2.getFixedValue(),123,.0001);
        System.out.println(read2);

        //but how does it work in the a more complicated structure?
        final DiffusingLogisticFactory biologyInitializer = new DiffusingLogisticFactory();
        final DiffusingLogisticFactory initializerLoaded = (DiffusingLogisticFactory)
                yaml.load("!!uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory\n" +
                                              "carryingCapacity: 12.0\n" +
                                              "differentialPercentageToMove: 5.0E-4\n" +
                                              "steepness: 0.8\n" +
                                              "percentageLimitOnDailyMovement: 0.01");
        //carrying capacity ought to be 12.0 Fixed carrying capacity
        Assert.assertTrue(initializerLoaded.getCarryingCapacity() instanceof FixedDoubleParameter);
        Assert.assertEquals(((FixedDoubleParameter)initializerLoaded.getCarryingCapacity()).getFixedValue(),12.0,.001);

        //and does it work if I use the algorithm factory name as well?
        final PrototypeScenario prototypeScenario = (PrototypeScenario)
                yaml.load("!!uk.ac.ox.oxfish.model.scenario.PrototypeScenario\n" +
                        //here i am just using the constructor name as in the CONSTRUCTOR_MAP and then a simple map
                                  "biologyInitializer:\n" +
                                  "  Diffusing Logistic:\n"+
                                  "    carryingCapacity: 14.0\n" +
                                  "    differentialPercentageToMove: 5.0E-4\n" +
                                  "    steepness: 0.7\n" +
                                  "    percentageLimitOnDailyMovement: 0.01\n" +
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
                                  "catchabilityMean: !!uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter\n" +
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
                                  "width: 50");


        final DiffusingLogisticFactory initializer = (DiffusingLogisticFactory) prototypeScenario.getBiologyInitializer();
        Assert.assertTrue(initializer.getCarryingCapacity() instanceof FixedDoubleParameter);
        Assert.assertEquals(((FixedDoubleParameter) initializer.getSteepness()).getFixedValue(), 0.7, .0001);
        Assert.assertEquals(((FixedDoubleParameter)initializer.getCarryingCapacity()).getFixedValue(),14.0,.0001);


    }
}
