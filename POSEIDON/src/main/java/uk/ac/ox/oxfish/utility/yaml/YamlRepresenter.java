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
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import uk.ac.ox.oxfish.model.scenario.Scenario;
import uk.ac.ox.oxfish.model.scenario.Scenarios;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

/**
 * The customized representer YAML object, useful to show pretty yaml output. In reality this performs a something of a
 * intermediate step because to beautify it further we remove all the tags
 * Created by carrknight on 7/10/15.
 */
class YamlRepresenter extends Representer {

    YamlRepresenter() {

        super(new DumperOptions());

        //go through all the double parameters and make them print as a single line "pretty" format

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

        this.representers.put(
            NormalDoubleParameter.class,
            data -> {
                final NormalDoubleParameter normal = (NormalDoubleParameter) data;
                return representData("normal " + normal.getMean() + " " + normal.getStandardDeviation());

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

        //do the same for the Path class
        final Represent pathRepresenter = data -> {
            final Path path = (Path) data;
            return representData(path.toString());
        };
        this.multiRepresenters.put(
            Path.class,
            pathRepresenter
        );

        //do the same for the coordinate class
        this.representers.put(
            Coordinate.class,
            data -> {
                final Coordinate data1 = (Coordinate) data;
                return representData(data1.x + "," + data1.y);
            }
        );


        //get a reference to this we can use from the outside
        final YamlRepresenter outer = this;
        //get all the algorithm factories including the super-class
        @SuppressWarnings("rawtypes") final List<Class<? extends AlgorithmFactory>> allAlgorithmFactories =
            Stream.concat(
                AlgorithmFactories.getAllAlgorithmFactories(),
                Stream.of(AlgorithmFactory.class)
            ).collect(toList());
        //for each class create a representer that shows it as a map
        for (final Class<?> c : allAlgorithmFactories) {
            this.addClassTag(c, Tag.MAP);
            this.representers.put(c, data -> {
                //prepare the node
                final Set<Property> properties = getProperties(data.getClass());
                //if you have no properties don't bother making a map, just return your full name
                if (properties.size() == 0)
                //just return your name in the constructor master-list as a string
                {
                    return outer.representData(AlgorithmFactories.nameLookup(c));
                }

                //otherwise print as map
                //first prepare the "value" which is just a node map representing our properties
                final List<NodeTuple> value = new ArrayList<>(properties.size());
                //tag yourself as MAP, which means there will be no visible tag but just "name":
                final Tag tag = Tag.MAP;
                //create the holding node
                final MappingNode node = new MappingNode(tag, value, BLOCK);
                //here's the trick: this mapping contains a single node which is just the name of this factory
                //in the constructor master list and then all the java-bean magic is a submap.
                value.add(new NodeTuple(
                    outer.representData(AlgorithmFactories.nameLookup(c)),
                    representJavaBean(properties, data)
                ));
                return node;
            });
        }

        //get all the scenarios
        final Iterable<Supplier<Scenario>> scenarios = new LinkedList<>(Scenarios.SCENARIOS.values());
        //for each scenario create a representer that shows it as a map
        for (final Supplier<Scenario> s : scenarios) {
            this.addClassTag(s.get().getClass(), Tag.MAP);

            this.representers.put(
                s.get().getClass(),
                data -> {
                    final Node node;

                    //prepare the node
                    final Set<Property> properties;

                    properties = getProperties(s.get().getClass());

                    //if you have no properties don't bother making a map, just return your full name
                    if (properties.size() == 0)
                    //just return your name in the constructor master-list as a string
                    {
                        node = outer.representData(data);
                    } else {
                        //otherwise print as map
                        //first prepare the "value" which is just a node map representing our properties
                        final List<NodeTuple> value = new ArrayList<>(
                            properties.size());
                        //tag yourself as MAP, which means there will be no visible tag but just "name":
                        final Tag tag = Tag.MAP;
                        //create the holding node
                        node = new MappingNode(tag, value, BLOCK);
                        //here's the trick: this mapping contains a single node which is just the name of this factory
                        //in the constructor master list and then all the java-bean magic is a submap.
                        value.add(new NodeTuple(
                            outer.representData(Scenarios.SCENARIOS.inverse().get(s)),
                            representJavaBean(properties, data)
                        ));
                    }
                    return node;
                }
            );

        }


    }
}





