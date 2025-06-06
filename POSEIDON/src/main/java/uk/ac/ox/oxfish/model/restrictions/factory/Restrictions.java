/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.model.restrictions.factory;

import uk.ac.ox.oxfish.model.restrictions.Restriction;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A list of possible communal restrictions types.
 *
 * @author Brian Powers 5/17/2019
 */

public class Restrictions {

    public static final Map<String, Supplier<AlgorithmFactory<? extends Restriction>>> CONSTRUCTORS =
        new LinkedHashMap<>();

    public static final Map<Class<? extends AlgorithmFactory<?>>, String> NAMES =
        new LinkedHashMap<>();

    static {
        CONSTRUCTORS.put("One Religious Holiday", OneReligiousHolidayFactory::new);
        NAMES.put(OneReligiousHolidayFactory.class, "One Religious Holiday");

    }
}
