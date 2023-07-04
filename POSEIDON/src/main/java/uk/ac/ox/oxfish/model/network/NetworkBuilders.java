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

package uk.ac.ox.oxfish.model.network;

import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The collection of all constructors
 * Created by carrknight on 7/1/15.
 */
public class NetworkBuilders {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String, Supplier<NetworkBuilder>> CONSTRUCTORS =
        new LinkedHashMap<>();

    public static final LinkedHashMap<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();

    static {

        CONSTRUCTORS.put(
            "No Network",
            EmptyNetworkBuilder::new
        );
        NAMES.put(EmptyNetworkBuilder.class, "No Network");
        CONSTRUCTORS.put(
            "Barabasi-Albert",
            BarabasiAlbertBuilder::new
        );
        NAMES.put(BarabasiAlbertBuilder.class, "Barabasi-Albert");
        CONSTRUCTORS.put(
            "Equal Out Degree",
            EquidegreeBuilder::new
        );
        NAMES.put(EquidegreeBuilder.class, "Equal Out Degree");
        CONSTRUCTORS.put(
            "Same Size Clubs",
            ClubNetworkBuilder::new
        );
        NAMES.put(ClubNetworkBuilder.class, "Same Size Clubs");

    }


}
