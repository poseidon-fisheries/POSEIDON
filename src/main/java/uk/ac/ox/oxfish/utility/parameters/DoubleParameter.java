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

import ec.util.MersenneTwisterFast;

import java.util.function.Function;

/**
 * Strategy/Scenario factories sometimes get called multiple times and often we'd like each time they are called to return
 * a slightly different value: for example we'd like each FishUntilFull strategy to have a different minimumPercentageFull
 * each time is created.
 * To do that we use DoubleParameter (or one of its cognate) which is just a supplier of double values
 * Created by carrknight on 6/7/15.
 */
public interface DoubleParameter extends Function<MersenneTwisterFast,Double>
{


    static DoubleParameter parseDoubleParameter(String nodeContent) {
        //trim and split
        final String[] split = nodeContent.trim().replaceAll("(')|(\")", "").split("\\s+");

        if(split[0].toLowerCase().trim().equals("nullparameter"))
            return new NullParameter();

        if(split.length == 1)
            //fixed
            return new FixedDoubleParameter(Double.parseDouble(split[0]));

        if(split[0].toLowerCase().equals("normal"))
            return new NormalDoubleParameter(Double.parseDouble(split[1]), Double.parseDouble(split[2]));

        if(split[0].toLowerCase().equals("uniform"))
            return new UniformDoubleParameter(Double.parseDouble(split[1]), Double.parseDouble(split[2]));

        if(split[0].toLowerCase().equals("sin"))
            return new SinusoidalDoubleParameter(Double.parseDouble(split[1]), Double.parseDouble(split[2]));

        if(split[0].toLowerCase().equals("select"))
            return new SelectDoubleParameter(nodeContent.trim().replace("select",""));

        throw new IllegalArgumentException("Do not recognize this double parameter!");

    }

    DoubleParameter makeCopy();
}
