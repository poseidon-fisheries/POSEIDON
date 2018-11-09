/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2017  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.utility.yaml;

import com.vividsolutions.jts.geom.Coordinate;
import org.jetbrains.annotations.NotNull;
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



        //add the ability to read/write coordinates

/*
        this.yamlConstructors.put(new Tag("!coord"), new Construct() {
            @Override
            public Object construct(Node node) {
                String val = (String) constructScalar((ScalarNode) node);
                return convertToCoordinate(val);

            }

            @Override
            public void construct2ndStep(Node node, Object object) {

            }
        });

*/

        //intercept the scalar nodes to see if they are actually Factories or DoubleParameters
        this.yamlClassConstructors.put(
                NodeId.scalar, new Constructor.ConstructScalar(){
                    @Override
                    public Object construct(Node nnode) {
                        //if field you are trying to fill is a coordinate
                        /*
                        if(nnode.getType().equals(Coordinate.class))
                        {
                            String val = (String) constructScalar((ScalarNode) nnode);
                            return convertToCoordinate(val);

                        }
                        */

                        //if the field you are trying to fill is a double parameter
                        if(nnode.getType().equals(DoubleParameter.class))
                            //then a simple scalar must be a fixed double parameter. Build it
                            return doubleParameterSplit((ScalarNode) nnode);
                        //if it's a path type we write and read it as string rather than with the ugly !! notation
                        if(nnode.getType().equals(Path.class))
                            return Paths.get(((ScalarNode) nnode).getValue());
                        //it's also possible that the scalar is an algorithm factory without any settable field
                        //this is rare since factories are represented as maps, but this might be one of the simple
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
                    //try super constructor first, most of the time it works
                    try {
                        return super.construct(node);
                    } catch (YAMLException e) {
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
                                Scenarios.SCENARIOS.get(((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue()).get();

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

                if(((MappingNode) node).getValue().size()>0) {
                    //again for policy scripts
                    if (PolicyScript.class.isAssignableFrom(node.getType()) ||
                            Objects.equals(
                                    ((ScalarNode) ((MappingNode) node).getValue().get(0).getKeyNode()).getValue(),
                                    "PolicyScript")) {

                        PolicyScript script = new PolicyScript();


                        //now we can deal with filling it through beans
                        //first allocate subnodes correctly
                        ((MappingNode) node).setValue(
                                ((MappingNode) ((MappingNode) node).getValue().get(0).getValueNode()).getValue());
                        //set type correctly
                        node.setType(PolicyScript.class);
                        constructJavaBean2ndStep((MappingNode) node, script);
                        return script;


                    }


                    if (PolicyScripts.class.isAssignableFrom(node.getType())) {

                        //now we can deal with filling it through beans
                        //first allocate subnodes correctly

                        //set type correctly

                        node.setType(PolicyScripts.class);
                        for (NodeTuple partialScript : ((MappingNode) ((MappingNode) node).getValue().get(
                                0).getValueNode()).getValue()) {
                            partialScript.getKeyNode().setType(Integer.class);
                            partialScript.getValueNode().setType(PolicyScript.class);
                        }
                        PolicyScripts script = new PolicyScripts();
                        constructJavaBean2ndStep((MappingNode) node, script);
                        return script;


                    }
                }

                return super.construct(node);
            }
        });
    }

    @NotNull
    public static Coordinate convertToCoordinate(String val) {
        String[] split = val.replaceAll("x:","").replaceAll("y:","").split(",");
        return new Coordinate(
                Double.parseDouble(split[0].trim().replaceAll("'","").replaceAll("\"","")),
                Double.parseDouble(split[1].trim().replaceAll("'","").replaceAll("\"","")));
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

        if(split[0].toLowerCase().trim().equals("nullparameter"))
            return new NullParameter();

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
