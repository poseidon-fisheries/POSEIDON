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

package uk.ac.ox.oxfish.biology.weather.initializer.factory;

import uk.ac.ox.oxfish.biology.weather.initializer.WeatherInitializer;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

/**
 * The collection of all factories that build weather initalizers
 * Created by carrknight on 9/8/15.
 */
public class WeatherInitializers {

    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final LinkedHashMap<String, Supplier<AlgorithmFactory<? extends WeatherInitializer>>> CONSTRUCTORS;
    public static final LinkedHashMap<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(ConstantWeatherFactory.class, "Constant Weather");
        NAMES.put(OscillatingWeatherFactory.class, "Oscillating Weather");
        NAMES.put(TimeSeriesWeatherFactory.class, "CSV Fixed Weather");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    /**
     * can't be instantiated
     */
    private WeatherInitializers() { }
}
