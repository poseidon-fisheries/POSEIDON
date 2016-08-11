package uk.ac.ox.oxfish.utility.yaml;

import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import uk.ac.ox.oxfish.model.scenario.PolicyScript;
import uk.ac.ox.oxfish.model.scenario.PolicyScripts;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.Scenarios;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * Constructor useful to implement YAML objects back into the Fishstate. I modify it so that it does the following things:
 * <ul>
 *     <li> FixedDoubleParameters can be input as numbers and it is still valid</li>
 *     <li> Algorithm Factories can be input as map and it's still valid</li>
 * </ul>
 * Created by carrknight on 7/10/15.
 */
public class YamlConstructor extends  Constructor {


    public YamlConstructor()
    {

        //intercept the scalar nodes to see if they are actually Factories or DoubleParameters
        this.yamlClassConstructors.put(NodeId.scalar, new Constructor.ConstructScalar(){
            @Override
            public Object construct(Node nnode) {
                //if the field you are trying to fill is a double parameter
                if(nnode.getType().equals(DoubleParameter.class))
                    //then a simple scalar must be a fixed double parameter. Build it
                    return doubleParameterSplit((ScalarNode) nnode);
                else
                    //if it's a path type we write and read it as string rather than with the ugly !! notation
                    if(nnode.getType().equals(Path.class))
                        return Paths.get(((ScalarNode) nnode).getValue());
                    else
                        //it's also possible that the scalar is an algorithm factory without any settable field
                        //this is rare which means that factories are represented as maps, but this might be one of the simple
                        //ones like AnarchyFactory
                        if(AlgorithmFactory.class.isAssignableFrom(nnode.getType()))
                            return AlgorithmFactories.constructorLookup((String) constructScalar((ScalarNode) nnode));
                            //otherwise I guess it's really a normal scalar!
                        else
                            return super.construct(nnode);                }
        });

        //intercept maps as well, some of them could be factories
        this.yamlClassConstructors.put(NodeId.mapping, new Constructor.ConstructMapping(){

            @Override
            public Object construct(Node node) {
                if(AlgorithmFactory.class.isAssignableFrom(node.getType())) {

                    //the original construct failed, hopefully this means it's an algorithm factory
                    //written as a map, so get its name and look it up
                    final AlgorithmFactory toReturn = AlgorithmFactories.constructorLookup(
                            ((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue());
                    //now take all the elements of the submap, we are going to place them by setter
                    //todo might have to flatten here!
                    ((MappingNode) node).setValue(
                            ((MappingNode)((MappingNode) node).getValue().get(0).getValueNode()).getValue());
                    assert toReturn != null;
                    //need to set the node to the correct return or the reflection magic of snakeYAML wouldn't work
                    node.setType(toReturn.getClass());
                    //use beans to set all the properties correctly
                    constructJavaBean2ndStep((MappingNode) node, toReturn);
                    //done!
                    return toReturn;

                }
                //try a similar approach for scenarios
                if(Scenario.class.isAssignableFrom(node.getType()))
                {
                    try{
                        //might have been written correctly as it is!
                        return super.construct(node);
                    }
                    catch (YAMLException e)
                    {
                        //this either means it's badly written somehow or more likely it's written in a "prettyfied" style

                        //grab first element, ought to be the name of the scenario
                        final  Scenario scenario =
                                Scenarios.SCENARIOS.get(((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue());

                        //now we can deal with filling it through beans
                        //first allocate subnodes correctly
                        ((MappingNode) node).setValue(
                                ((MappingNode) ((MappingNode) node).getValue().get(0).getValueNode()).getValue());
                        //set type correctly
                        node.setType(scenario.getClass());
                        constructJavaBean2ndStep((MappingNode) node,scenario);
                        return scenario;


                    }
                }

                //again for policy scripts
                if(PolicyScript.class.isAssignableFrom(node.getType()) ||
                        Objects.equals(((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue(),
                                       "PolicyScript"))
                {

                    //now we can deal with filling it through beans
                    //first allocate subnodes correctly
                    ((MappingNode) node).setValue(
                            ((MappingNode) ((MappingNode) node).getValue().get(0).getValueNode()).getValue());
                    //set type correctly
                    node.setType(PolicyScript.class);
                    PolicyScript script = new PolicyScript();
                    constructJavaBean2ndStep((MappingNode) node, script);
                    return script;



                }

                if(PolicyScripts.class.isAssignableFrom(node.getType()))
                {

                    //now we can deal with filling it through beans
                    //first allocate subnodes correctly

                    //set type correctly

                    node.setType(PolicyScripts.class);
                    for(NodeTuple partialScript : ((MappingNode)((MappingNode) node).getValue().get(0).getValueNode()).getValue())
                    {
                        partialScript.getKeyNode().setType(Integer.class);
                        partialScript.getValueNode().setType(PolicyScript.class);
                    }
                    PolicyScripts script = new PolicyScripts();
                    constructJavaBean2ndStep((MappingNode) node, script);
                    return script;



                }

                else
                    return super.construct(node);
            }
        });
    }




    private DoubleParameter doubleParameterSplit(ScalarNode node)
    {

        //get it as a string
        String nodeContent = (String) constructScalar(node);
        return parseDoubleParameter(nodeContent);


    }

    public static DoubleParameter parseDoubleParameter(String nodeContent) {
        //trim and split
        final String[] split = nodeContent.trim().replaceAll("(')|(\")", "").split("\\s+");
        if(split.length == 1)
            //fixed
            return new FixedDoubleParameter(Double.parseDouble(split[0]));

        if(split[0].toLowerCase().equals("normal"))
            return new NormalDoubleParameter(Double.parseDouble(split[1]), Double.parseDouble(split[2]));

        if(split[0].toLowerCase().equals("uniform"))
            return new UniformDoubleParameter(Double.parseDouble(split[1]), Double.parseDouble(split[2]));

        if(split[0].toLowerCase().equals("sin"))
            return new SinusoidalDoubleParameter(Double.parseDouble(split[1]), Double.parseDouble(split[2]));

        if(split[0].toLowerCase().equals("select"))
            return new SelectDoubleParameter(nodeContent.trim().replace("select",""));

        throw new IllegalArgumentException("Do not recognize this double parameter!");

    }


}
