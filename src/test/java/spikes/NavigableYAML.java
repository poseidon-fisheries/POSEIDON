package spikes;


import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeId;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import uk.ac.ox.oxfish.biology.initializer.BiologyInitializer;
import uk.ac.ox.oxfish.biology.initializer.factory.DiffusingLogisticFactory;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.regex.Pattern;

public class NavigableYAML {

    @Test
    public void writeAndReadYaml() throws FileNotFoundException {

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);


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
            this.yamlMultiConstructors.put("tag:yaml.org,2002:uk.ac.ox.oxfish.utility.parameters.DoubleParameter",
                                           constructor);
            this.yamlClassConstructors.put(NodeId.scalar,new ConstructorScalar() );
        }

        private class ConstructorScalar extends Constructor.ConstructScalar
        {
            @Override
            public Object construct(Node nnode) {
                if(nnode.getType().equals(DoubleParameter.class))
                    return new FixedDoubleParameter((Double.parseDouble((String) constructScalar((ScalarNode) nnode))));
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
    }


    @Test
    public void implicitFixedValue() throws Exception {

        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(new FixedValueCostructor(),new FixedValueRepresenter(),options);
        FixedDoubleParameter fixed = new FixedDoubleParameter(123);
        String dump = yaml.dump(fixed);
        System.out.println(dump);
        final FixedDoubleParameter read = (FixedDoubleParameter) yaml.load(dump);
        Assert.assertEquals(read.getFixedValue(),123,.0001);

        //try to make it implicit
        yaml.addImplicitResolver(new Tag("!fixed"), Pattern.compile("[-+]?[0-9]*\\.?[0-9]+([eE][-+]?[0-9]+)?"), "123456789");

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
                                              "maxSteepness: 0.8\n" +
                                              "minSteepness: 0.6\n" +
                                              "percentageLimitOnDailyMovement: 0.01");

        Assert.assertTrue(initializerLoaded.getCarryingCapacity() instanceof FixedDoubleParameter);
        Assert.assertEquals(((FixedDoubleParameter)initializerLoaded.getCarryingCapacity()).getFixedValue(),12.0,.001);


    }
}
