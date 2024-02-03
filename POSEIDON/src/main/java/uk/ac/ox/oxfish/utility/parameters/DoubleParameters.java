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

package uk.ac.ox.oxfish.utility.parameters;

import uk.ac.ox.poseidon.common.api.parameters.*;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Just a collection of the doubleParameters that are available; useful for gui and instantiation Created by carrknight
 * on 6/7/15.
 */
public class DoubleParameters {

    public static final Map<String, Supplier<DoubleParameter>> DOUBLE_PARAMETERS;
    public static final Map<Class<? extends DoubleParameter>, String> DOUBLE_PARAMETERS_NAME;

    static {
        final HashMap<String, Supplier<DoubleParameter>> parameters = new HashMap<>();
        final HashMap<Class<? extends DoubleParameter>, String> names = new HashMap<>();
        parameters.put("Fixed", () -> new FixedDoubleParameter(0));
        names.put(FixedDoubleParameter.class, "Fixed");
        parameters.put("Normal", () -> new NormalDoubleParameter(0, 1));
        names.put(NormalDoubleParameter.class, "Normal");
        parameters.put("Uniform", () -> new UniformDoubleParameter(0, 1));
        names.put(UniformDoubleParameter.class, "Uniform");
        parameters.put("Beta", () -> new BetaDoubleParameter(1, 1));
        names.put(BetaDoubleParameter.class, "Beta");
        parameters.put("Select", () -> new SelectDoubleParameter("0 1"));
        names.put(SelectDoubleParameter.class, "Select");
        parameters.put("Sin", () -> new SinusoidalDoubleParameter(1, 0.01));
        names.put(SelectDoubleParameter.class, "Sin");

        parameters.put("Conditional", () -> new ConditionalDoubleParameter(
            false, new FixedDoubleParameter(0)));
        names.put(ConditionalDoubleParameter.class, "Conditional");

        parameters.put("NullParameter", () -> new NullParameter());
        names.put(NullParameter.class, "NullParameter");

        DOUBLE_PARAMETERS = Collections.unmodifiableMap(parameters);
        DOUBLE_PARAMETERS_NAME = Collections.unmodifiableMap(names);

    }

}
