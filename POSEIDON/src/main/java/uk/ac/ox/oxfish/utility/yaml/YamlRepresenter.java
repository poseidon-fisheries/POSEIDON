/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2024 CoHESyS Lab cohesys.lab@gmail.com
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

import com.google.common.collect.ImmutableList;
import com.vividsolutions.jts.geom.Coordinate;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import uk.ac.ox.oxfish.model.scenario.ScenarioSupplier;
import uk.ac.ox.oxfish.utility.parameters.*;
import uk.ac.ox.poseidon.common.api.FactorySupplier;
import uk.ac.ox.poseidon.common.core.parameters.DateParameter;
import uk.ac.ox.poseidon.common.core.parameters.FixedParameter;

import java.io.File;
import java.nio.file.Path;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

/**
 * The customized representer YAML object, useful to show pretty yaml output. In reality this
 * performs a something of a intermediate step because to beautify it further we remove all the tags
 * Created by carrknight on 7/10/15.
 */
class YamlRepresenter extends Representer {

    YamlRepresenter() {

        super(new DumperOptions());

        this.multiRepresenters.put(
            DateParameter.class,
            data -> representData(((FixedParameter<?>) data).getValue().toString())
        );

        this.multiRepresenters.put(
            FixedParameter.class,
            data -> representData(((FixedParameter<?>) data).getValue())
        );

        this.multiRepresenters.put(
            CalibratedParameter.class,
            data -> representData(((CalibratedParameter) data).getDefaultValue())
        );

        this.representers.put(
            NullParameter.class,
            data -> representData("nullparameter")
        );

        // go through all the double parameters and make them print as a single line "pretty" format

        this.representers.put(
            NormalDoubleParameter.class,
            data -> {
                final NormalDoubleParameter normal = (NormalDoubleParameter) data;
                return representData("normal " +
                    normal.getMean() +
                    " " +
                    normal.getStandardDeviation());

            }
        );

        this.representers.put(
            UniformDoubleParameter.class,
            data -> {
                final UniformDoubleParameter normal = (UniformDoubleParameter) data;
                return representData("uniform " + normal.getMinimum() + " " + normal.getMaximum());
            }
        );

        this.representers.put(
            SelectDoubleParameter.class,
            data -> {
                final SelectDoubleParameter select = (SelectDoubleParameter) data;
                return representData("select " + select.getValueString());
            }
        );

        this.representers.put(
            SinusoidalDoubleParameter.class,
            data -> {
                final SinusoidalDoubleParameter sin = (SinusoidalDoubleParameter) data;
                return representData("sin " + sin.getAmplitude() + " " + sin.getFrequency());
            }
        );

        // do the same for the Path class
        final Represent pathRepresenter = data -> {
            final Path path = (Path) data;
            // make sure path separators are forward slashes even on Windows
            return representData(path.toString().replace('/', File.separatorChar));
        };
        this.multiRepresenters.put(
            Path.class,
            pathRepresenter
        );

        // do the same for the coordinate class
        this.representers.put(
            Coordinate.class,
            data -> {
                final Coordinate data1 = (Coordinate) data;
                return representData(data1.x + "," + data1.y);
            }
        );

        addMapRepresenters(
            FactorySupplier.class,
            FactorySupplier::getFactoryClass,
            FactorySupplier::getFactoryName
        );

        addMapRepresenters(
            ScenarioSupplier.class,
            scenarioSupplier -> scenarioSupplier.get().getClass(),
            ScenarioSupplier::getScenarioName
        );
    }

    /**
     * Classes that are loaded as services (i.e., scenarios and component factories) are represent
     * as maps in the YAML file. This function add representers for them.
     *
     * @param supplierClass        The class of supplier made available through the service loader
     *                             (e.g., {@link ScenarioSupplier} or {@link FactorySupplier}).
     * @param objectClassExtractor A function to get the target object class from the supplier.
     * @param nameExtractor        A function to get the name of the target object from the
     *                             supplier.
     * @param <T>                  The type of the target object.
     * @param <S>                  The type of the supplier.
     */
    private <T, S extends Supplier<T>> void addMapRepresenters(
        final Class<? extends S> supplierClass,
        final Function<? super S, Class<? extends T>> objectClassExtractor,
        final Function<? super S, String> nameExtractor
    ) {
        // We loop through all the available suppliers of the desired class,
        // and we add a representer for each of them.
        ServiceLoader.load(supplierClass).forEach(supplier -> {
            final Class<? extends T> objectClass = objectClassExtractor.apply(supplier);
            final String name = nameExtractor.apply(supplier);
            this.addClassTag(objectClass, Tag.MAP);
            this.representers.put(objectClass, data -> {
                final Set<Property> properties = getProperties(data.getClass());
                final Node nameNode = YamlRepresenter.this.representData(name);
                if (properties.isEmpty())
                    // If the object class has no properties, we just encode the name.
                    return nameNode;
                else
                    // otherwise, we represent it as a map with a single entry, using the object
                    // name as a key and the set of properties as the value.
                    return new MappingNode(
                        Tag.MAP,
                        ImmutableList.of(new NodeTuple(
                            nameNode,
                            representJavaBean(properties, data)
                        )),
                        BLOCK
                    );
            });
        });

    }
}





