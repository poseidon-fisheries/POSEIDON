package uk.ac.ox.oxfish.utility.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.NormalDoubleParameter;
import uk.ac.ox.oxfish.utility.parameters.UniformDoubleParameter;

/**
 * The customized representer YAML object, useful to show pretty yaml output. In reality this performs a something of a
 * intermediate step because to beautify it further we remove all the tags
 * Created by carrknight on 7/10/15.
 */
public class YamlRepresenter extends Representer
{

    public YamlRepresenter()
    {



        this.representers.put(FixedDoubleParameter.class,
                              data -> representScalar(new Tag("!fixed"),
                                                      String.valueOf(((FixedDoubleParameter) data).getFixedValue())));


        this.representers.put(NormalDoubleParameter.class,
                              data -> {
                                  final NormalDoubleParameter normal = (NormalDoubleParameter) data;
                                  return representScalar(new Tag("!normal"),
                                                         normal.getMean() + " " + normal.getStandardDeviation()
                                                                 );
                              });


        this.representers.put(UniformDoubleParameter.class,
                              data -> {
                                  final UniformDoubleParameter normal = (UniformDoubleParameter) data;
                                  return representScalar(new Tag("!uniform"),
                                                         normal.getMinimum() + " " + normal.getMaximum()
                                  );
                              });
    }
}
