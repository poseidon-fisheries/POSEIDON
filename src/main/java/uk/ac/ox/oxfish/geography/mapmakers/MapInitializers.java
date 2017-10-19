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

package uk.ac.ox.oxfish.geography.mapmakers;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A list of all constructors for the map initializers
 * Created by carrknight on 11/5/15.
 */
public class MapInitializers {


    private MapInitializers() {
    }

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String,Supplier<AlgorithmFactory<? extends MapInitializer>>> CONSTRUCTORS =
            new LinkedHashMap<>();
    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>,String> NAMES =
            new LinkedHashMap<>();

    static {
        CONSTRUCTORS.put("Simple Map",
                         SimpleMapInitializerFactory::new);
        NAMES.put(SimpleMapInitializerFactory.class,"Simple Map");

        CONSTRUCTORS.put("OSMOSE Map",
                         OsmoseMapInitializerFactory::new);
        NAMES.put(OsmoseMapInitializerFactory.class,"OSMOSE Map");


        CONSTRUCTORS.put("OSMOSE Bounded Map",
                         OsmoseBoundedMapInitializerFactory::new);
        NAMES.put(OsmoseBoundedMapInitializerFactory.class,"OSMOSE Bounded Map");

        CONSTRUCTORS.put("From File Map",
                         FromFileMapInitializerFactory::new);
        NAMES.put(FromFileMapInitializerFactory.class,"From File Map");


    }




}
