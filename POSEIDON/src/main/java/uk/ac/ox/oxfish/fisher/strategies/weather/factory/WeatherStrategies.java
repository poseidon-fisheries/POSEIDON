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

package uk.ac.ox.oxfish.fisher.strategies.weather.factory;

import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * All the factories that build weather emergency strategies
 * Created by carrknight on 9/8/15.
 */
public class WeatherStrategies {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String, Supplier<AlgorithmFactory<? extends WeatherEmergencyStrategy>>> CONSTRUCTORS;

    public static final LinkedHashMap<Class<? extends AlgorithmFactory<?>>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(IgnoreWeatherFactory.class, "Ignore Weather");
        NAMES.put(WindThresholdFactory.class, "Sail up to Threshold");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }
}
