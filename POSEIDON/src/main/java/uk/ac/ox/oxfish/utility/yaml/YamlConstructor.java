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
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.inspector.TrustedTagInspector;
import org.yaml.snakeyaml.nodes.*;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.DoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkNotNull;
import static uk.ac.ox.oxfish.model.scenario.Scenarios.SCENARIOS;

/**
 * Constructor useful to implement YAML objects back into the Fishstate. I modify it so that it does the following things:
 * <ul>
 *     <li> FixedDoubleParameters can be input as numbers and it is still valid</li>
 *     <li> Algorithm Factories can be input as map and it's still valid</li>
 * </ul>
 * Created by carrknight on 7/10/15.
 */
public class YamlConstructor extends Constructor {

    private static final LoaderOptions LOADER_OPTIONS = new LoaderOptions();

    static {
        // Allow using global tags
        LOADER_OPTIONS.setTagInspector(new TrustedTagInspector());
    }

    YamlConstructor() {

        super(LOADER_OPTIONS);

        //intercept the scalar nodes to see if they are actually Factories or DoubleParameters
        this.yamlClassConstructors.put(
            NodeId.scalar, new Constructor.ConstructScalar() {
                @Override
                public Object construct(final Node node) {
                    //if the field you are trying to fill is a double parameter
                    if (node.getType().equals(DoubleParameter.class))
                        //then a simple scalar must be a fixed double parameter. Build it
                        return doubleParameterSplit((ScalarNode) node);
                    //if it's a path type we write and read it as string rather than with the ugly !! notation
                    if (node.getType().equals(Path.class))
                        return Paths.get(((ScalarNode) node).getValue());
                    //it's also possible that the scalar is an algorithm factory without any settable field
                    //this is rare since factories are represented as maps, but this might be one of the simple
                    //ones like AnarchyFactory
                    if (AlgorithmFactory.class.isAssignableFrom(node.getType()))
                        return AlgorithmFactories.constructorLookup(constructScalar((ScalarNode) node));
                        //otherwise I guess it's really a normal scalar!
                    else
                        // other FixedParameter subclasses will be handled here, as
                        // SnakeYAML is able to identify the one-argument constructor
                        // needed to build the right objects
                        return super.construct(node);
                }
            });

        //intercept maps as well, some of them could be factories
        this.yamlClassConstructors.put(NodeId.mapping, new Constructor.ConstructMapping() {
            @Override
            public Object construct(final Node node) {
                try {
                    // First try to construct the object normally
                    return super.construct(node);
                } catch (final YAMLException e) {
                    // If it fails, it might be a factory or a scenario that has been
                    // written as a map for readability, so try to construct it that way
                    final NodeTuple nodeTuple = ((MappingNode) node).getValue().get(0);
                    if (AlgorithmFactory.class.isAssignableFrom(node.getType())) {
                        return constructNamedObject(nodeTuple, AlgorithmFactories::constructorLookup);
                    } else if (Scenario.class.isAssignableFrom(node.getType())) {
                        return constructNamedObject(nodeTuple, name -> SCENARIOS.get(name).get());
                    } else {
                        // If it's neither an algorithm factory nor a scenario, propagate the exception
                        throw e;
                    }
                }
            }

            private Object constructNamedObject(
                final NodeTuple nodeTuple,
                final Function<? super String, Object> constructor
            ) {
                //the original construct failed, hopefully this means it's an algorithm factory
                //written as a map, so get its name and look it up
                final String name = ((ScalarNode) nodeTuple.getKeyNode()).getValue();
                final Object object = checkNotNull(constructor.apply(name));
                // The top level mapping node is only there to identify the right
                // factory. The actual construction is done using the value node.
                final MappingNode valueNode = (MappingNode) nodeTuple.getValueNode();
                //need to set the node to the correct return or the reflection magic of snakeYAML wouldn't work
                valueNode.setType(object.getClass());
                //use beans to set all the properties correctly
                constructJavaBean2ndStep(valueNode, object);
                //done!
                return object;
            }
        });
    }

    private DoubleParameter doubleParameterSplit(final ScalarNode node) {
        //get it as a string
        final String nodeContent = constructScalar(node);
        return DoubleParameter.parseDoubleParameter(nodeContent);
    }

    public static Coordinate convertToCoordinate(final String val) {
        final String[] split = val.replaceAll("x:", "").replaceAll("y:", "").split(",");
        return new Coordinate(
            parseCoordinateString(split[0]),
            parseCoordinateString(split[1])
        );
    }

    private static double parseCoordinateString(final String coordinateString) {
        return Double.parseDouble(coordinateString.trim().replaceAll("'", "").replaceAll("\"", ""));
    }

}
