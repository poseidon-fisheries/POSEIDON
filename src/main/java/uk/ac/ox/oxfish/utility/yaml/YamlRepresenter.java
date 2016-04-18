package uk.ac.ox.oxfish.utility.yaml;

import org.yaml.snakeyaml.error.YAMLException;
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

import java.beans.IntrospectionException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * The customized representer YAML object, useful to show pretty yaml output. In reality this performs a something of a
 * intermediate step because to beautify it further we remove all the tags
 * Created by carrknight on 7/10/15.
 */
public class YamlRepresenter extends Representer
{

    public YamlRepresenter() {

        //go through all the double parameters and make them print as a single line "pretty" format
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


        this.representers.put(SelectDoubleParameter.class,
                              data -> {
                                  final SelectDoubleParameter select = (SelectDoubleParameter) data;
                                  return
                                          representData("select " +
                                                                select.getValueString());
                              });


        this.representers.put(SinusoidalDoubleParameter.class,
                              data -> {
                                  final SinusoidalDoubleParameter sin = (SinusoidalDoubleParameter) data;
                                  return
                                          representData("sin " +
                                                                sin.getAmplitude() + " " +
                                                                sin.getFrequency());
                              });

        //do the same for the Path class
        Represent pathRepresenter = data -> {
            final Path path = (Path) data;
            return
                    representData(path.toString());
        };
        this.multiRepresenters.put(Path.class,
                              pathRepresenter);



        //get a reference to this we can use from the outside
        final YamlRepresenter outer = this;
        //get all the algorithm factories
        final List<Class<? extends AlgorithmFactory>> allAlgorithmFactories = AlgorithmFactories.getAllAlgorithmFactories();
        //including the super-class
        allAlgorithmFactories.add(AlgorithmFactory.class); //add the super class
        //for each class create a representer that shows it as a map
        for (Class<? extends AlgorithmFactory> c : allAlgorithmFactories) {
            this.addClassTag(c, Tag.MAP);
            this.representers.put(c, new Represent() {
                @Override
                public Node representData(Object data) {
                    try {
                        //prepare the node
                        final Set<Property> properties = getProperties(data.getClass());
                        //if you have no properties don't bother making a map, just return your full name
                        if (properties.size() == 0)
                            //just return your name in the constructor master-list as a string
                            return outer.representData(AlgorithmFactories.nameLookup(c));

                        //otherwise print as map
                        //first prepare the "value" which is just a node map representing our properties
                        List<NodeTuple> value = new ArrayList<>(properties.size());
                        //tag yourself as MAP, which means there will be no visible tag but just "name":
                        Tag tag = Tag.MAP;
                        //create the holding node
                        MappingNode node = new MappingNode(tag, value, false);
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


            //get all the scenarios
            final List<Scenario> scenarios = new LinkedList<>(Scenarios.SCENARIOS.values());
            //for each scenario create a representer that shows it as a map
            for(Scenario s : scenarios) {
                this.addClassTag(s.getClass(), Tag.MAP);

                this.representers.put(s.getClass(),
                                      new Represent() {
                                          @Override
                                          public Node representData(Object data) {
                                              try {
                                                  Node node;


                                                  //prepare the node
                                                  final Set<Property> properties;

                                                  properties = getProperties(s.getClass());


                                                  //if you have no properties don't bother making a map, just return your full name
                                                  if (properties.size() == 0)
                                                      //just return your name in the constructor master-list as a string
                                                      node = outer.representData(data);
                                                  else {
                                                      //otherwise print as map
                                                      //first prepare the "value" which is just a node map representing our properties
                                                      List<NodeTuple> value = new ArrayList<>(
                                                              properties.size());
                                                      //tag yourself as MAP, which means there will be no visible tag but just "name":
                                                      Tag tag = Tag.MAP;
                                                      //create the holding node
                                                      node = new MappingNode(tag, value, false);
                                                      //here's the trick: this mapping contains a single node which is just the name of this factory
                                                      //in the constructor master list and then all the java-bean magic is a submap.
                                                      value.add(new NodeTuple(
                                                              outer.representData(Scenarios.SCENARIOS.inverse().get(s)),
                                                              representJavaBean(properties, data)));
                                                  }
                                                  return node;
                                              } catch (IntrospectionException e)
                                              {
                                                  throw  new YAMLException(e);
                                              }}});

            }


    }
}





