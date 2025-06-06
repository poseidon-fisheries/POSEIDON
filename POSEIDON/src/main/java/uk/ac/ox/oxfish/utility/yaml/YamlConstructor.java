/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.utility.yaml;

import com.google.common.collect.ImmutableMap;
import com.vividsolutions.jts.geom.Coordinate;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.ScenarioSupplier;
import uk.ac.ox.oxfish.utility.parameters.*;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.api.GenericComponentFactory;
import uk.ac.ox.poseidon.common.api.parameters.DoubleParameter;
import uk.ac.ox.poseidon.common.core.parameters.DateParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.Streams.stream;
import static java.util.function.UnaryOperator.identity;

/**
 * Constructor useful to implement YAML objects back into the Fishstate. I modify it so that it does
 * the following things:
 * <ul>
 *     <li> FixedDoubleParameters can be input as numbers and it is still valid</li>
 *     <li> Algorithm Factories can be input as map and it's still valid</li>
 * </ul>
 * Created by carrknight on 7/10/15.
 */
public class YamlConstructor extends Constructor {

    private static final LoaderOptions LOADER_OPTIONS = new LoaderOptions();

    private final Map<String, FactorySupplier> factorySuppliers =
        makeSupplierMap(FactorySupplier.class, FactorySupplier::getFactoryName);

    private final Map<String, ScenarioSupplier> scenarioSuppliers =
        makeSupplierMap(ScenarioSupplier.class, ScenarioSupplier::getScenarioName);

    YamlConstructor() {

        super(LOADER_OPTIONS);

        // intercept the scalar nodes to see if they are actually Factories or DoubleParameters
        this.yamlClassConstructors.put(
            NodeId.scalar, new Constructor.ConstructScalar() {
                @Override
                public Object construct(final Node node) {
                    if (node.getType().equals(DateParameter.class))
                        return new DateParameter(LocalDate.parse(((ScalarNode) node).getValue()));
                    // if the field you are trying to fill is a double parameter
                    if (node.getType().equals(DoubleParameter.class))
                        // then a simple scalar must be a fixed double parameter. Build it
                        return doubleParameterSplit((ScalarNode) node);
                    // if it's a path type we write and read it as string rather than with the
                    // ugly !! notation
                    if (node.getType().equals(Path.class))
                        return Paths.get(((ScalarNode) node).getValue());
                    // it's also possible that the scalar is an algorithm factory without any
                    // settable field
                    // this is rare since factories are represented as maps, but this might be
                    // one of the simple
                    // ones like AnarchyFactory
                    if (GenericComponentFactory.class.isAssignableFrom(node.getType()))
                        return constructFactory(constructScalar((ScalarNode) node));
                        // otherwise I guess it's really a normal scalar!
                    else
                        // other FixedParameter subclasses will be handled here, as
                        // SnakeYAML is able to identify the one-argument constructor
                        // needed to build the right objects
                        return super.construct(node);
                }
            });

        // intercept maps as well, some of them could be factories
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
                    if (GenericComponentFactory.class.isAssignableFrom(node.getType())) {
                        return constructNamedObject(
                            nodeTuple,
                            YamlConstructor.this::constructFactory
                        );
                    } else if (Scenario.class.isAssignableFrom(node.getType())) {
                        return constructNamedObject(
                            nodeTuple,
                            YamlConstructor.this::constructScenario
                        );
                    } else {
                        // If it's neither an algorithm factory nor a scenario, propagate the
                        // exception
                        throw e;
                    }
                }
            }

            private Object constructNamedObject(
                final NodeTuple nodeTuple,
                final Function<? super String, Object> constructor
            ) {
                // the original construct failed, hopefully this means it's an algorithm factory
                // written as a map, so get its name and look it up
                final String name = ((ScalarNode) nodeTuple.getKeyNode()).getValue();
                final Object object = checkNotNull(constructor.apply(name));
                // The top level mapping node is only there to identify the right
                // factory. The actual construction is done using the value node.
                final MappingNode valueNode = (MappingNode) nodeTuple.getValueNode();
                // need to set the node to the correct return or the reflection magic of
                // snakeYAML wouldn't work
                valueNode.setType(object.getClass());
                // use beans to set all the properties correctly
                constructJavaBean2ndStep(valueNode, object);
                // done!
                return object;
            }
        });
    }

    private static <T, S extends Supplier<T>> ImmutableMap<String, S> makeSupplierMap(
        final Class<S> supplierClass,
        final Function<S, String> nameGetter
    ) {
        return stream(ServiceLoader.load(supplierClass)).collect(toImmutableMap(
            nameGetter,
            identity(),
            (supplier, __) -> {
                throw new IllegalStateException(
                    "Duplicate supplier name: " + nameGetter.apply(supplier)
                );
            }
        ));
    }

    public static DoubleParameter parseDoubleParameter(final String nodeContent) {
        // trim and split
        final String[] split = nodeContent.trim().replaceAll("(')|(\")", "").split("\\s+");

        if (split[0].toLowerCase().trim().equals("nullparameter"))
            return new NullParameter();

        if (split.length == 1)
            // fixed
            return new FixedDoubleParameter(Double.parseDouble(split[0]));

        if (split[0].equalsIgnoreCase("normal"))
            return new NormalDoubleParameter(
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2])
            );

        if (split[0].equalsIgnoreCase("uniform"))
            return new UniformDoubleParameter(
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2])
            );

        if (split[0].equalsIgnoreCase("sin"))
            return new SinusoidalDoubleParameter(
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2])
            );

        if (split[0].equalsIgnoreCase("select"))
            return new SelectDoubleParameter(nodeContent.trim().replace("select", ""));

        if (split[0].equalsIgnoreCase("beta"))
            return new BetaDoubleParameter(
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2])
            );

        if (split[0].equalsIgnoreCase("weibull"))
            return new WeibullDoubleParameter(
                Double.parseDouble(split[1]),
                Double.parseDouble(split[2])
            );

        if (split[0].equalsIgnoreCase("conditional"))
            return new ConditionalDoubleParameter(
                Boolean.parseBoolean(split[1]),
                parseDoubleParameter(split[2])
            );

        throw new IllegalArgumentException("Do not recognize this double parameter!");

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

    private DoubleParameter doubleParameterSplit(final ScalarNode node) {
        // get it as a string
        final String nodeContent = constructScalar(node);
        return parseDoubleParameter(nodeContent);
    }

    private GenericComponentFactory<?, ?> constructFactory(final String factoryName) {
        return Optional
            .ofNullable(factorySuppliers.get(factoryName))
            .map(Supplier::get)
            .orElseThrow(() -> new IllegalStateException(
                "Component factory not found: " + factoryName)
            );
    }

    private Scenario constructScenario(final String scenarioName) {
        return Optional
            .ofNullable(scenarioSuppliers.get(scenarioName))
            .map(Supplier::get)
            .orElseThrow(() -> new IllegalStateException(MessageFormat.format(
                "Scenario not found: {0}.\nAvailable scenarios are: \n{1}",
                scenarioName,
                String.join("\n", scenarioSuppliers.keySet())
            )));
    }

}
