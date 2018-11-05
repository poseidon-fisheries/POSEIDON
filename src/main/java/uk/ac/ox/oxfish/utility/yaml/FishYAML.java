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

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.TypeDescription;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;

import java.util.regex.Pattern;

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
        this.addImplicitResolver(new Tag("!coord"),
                                 Pattern.compile("[\\s'\"]*x:[0-9]+\\.?[0-9]*,y:[0-9]+\\.?[0-9]*[\\s'\"]*"),
                                 null);

     //   this.customConstructor.addTypeDescription(new TypeDescription())

    }

    private static DumperOptions dumperOptions() {
        DumperOptions options = new DumperOptions();
        options.setPrettyFlow(true);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
        return options;
    }








}
