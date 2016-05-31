package uk.ac.ox.oxfish.utility.yaml;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * A customized YAML reader to use with the model
 * Created by carrknight on 7/10/15.
 */
public class FishYAML extends Yaml{


    private final YamlConstructor customConstructor;


    /**
     * Create Yaml instance. It is safe to create a few instances and use them
     * in different Threads.
     */
    public FishYAML() {



        super(new YamlConstructor(), new YamlRepresenter(),dumperOptions());
        this.customConstructor = ((YamlConstructor) super.constructor);

    }

    private static DumperOptions dumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        return options;
    }








}
