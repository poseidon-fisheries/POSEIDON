package uk.ac.ox.oxfish.utility.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.FieldProperty;
import org.yaml.snakeyaml.introspector.GenericProperty;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import uk.ac.ox.oxfish.utility.AlgorithmFactories;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

import java.beans.IntrospectionException;
import java.util.*;

/**
 * The customized representer YAML object, useful to show pretty yaml output. In reality this performs a something of a
 * intermediate step because to beautify it further we remove all the tags
 * Created by carrknight on 7/10/15.
 */
public class YamlRepresenter extends Representer
{

    public YamlRepresenter()
    {




       // this.addClassTag(FixedDoubleParameter.class,Tag.MAP);
        this.representers.put(FixedDoubleParameter.class,
                              data -> representData(
                                                      String.valueOf(((FixedDoubleParameter) data).getFixedValue())));


        this.representers.put(NormalDoubleParameter.class,
                              data -> {
                                  final NormalDoubleParameter normal = (NormalDoubleParameter) data;
                                  return
                                          representData("normal " +
                                                        normal.getMean() + " " + normal.getStandardDeviation());

                              });


        this.representers.put(UniformDoubleParameter.class,
                              data -> {
                                  final UniformDoubleParameter normal = (UniformDoubleParameter) data;
                                  return
                                          representData("uniform " +
                                                                normal.getMinimum() + " " + normal.getMaximum());
                              });


        YamlRepresenter outer = this;
        this.addClassTag(AlgorithmFactory.class, Tag.MAP);
        for(Class<? extends AlgorithmFactory> c : AlgorithmFactories.getAllAlgorithmFactories())
        {
            this.addClassTag(c,Tag.MAP);
            this.representers.put(c, new Represent() {
                @Override
                public Node representData(Object data) {
                    try {
                        //prepare the node
                        final Set<Property> properties = getProperties(data.getClass());
                        //if you have no properties don't bother making a map, just return your full name
                        if(properties.size() == 0)
                            //just return your name in the constructor master-list as a string
                            return outer.representData(AlgorithmFactories.nameLookup(c));

                        //otherwise print as map
                        //first prepare the "value" which is just a node map representing our properties
                        List<NodeTuple> value = new ArrayList<NodeTuple>(properties.size());
                        //tag yourself as MAP, which means there will be no visible tag but just "name":
                        Tag tag = Tag.MAP;
                        //create the holding node
                        MappingNode node = new MappingNode(tag, value,false);
                        //here's the trick: this mapping contains a single node which is just the name of this factory
                        //in the constructor master list and then all the java-bean magic is a submap.
                        value.add(new NodeTuple(outer.representData(AlgorithmFactories.nameLookup(c)),
                                                representJavaBean(properties, data)));
                        return node;
                    } catch (IntrospectionException e) {
                        throw new YAMLException(e);
                    }
                }
            });
        }
    }

}
