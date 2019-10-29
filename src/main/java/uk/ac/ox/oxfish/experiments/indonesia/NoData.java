/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2019  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.experiments.indonesia;

import uk.ac.ox.oxfish.maximization.generic.SimpleOptimizationParameter;

import java.util.LinkedList;
import java.util.List;

/**
 * basically act as if this was a rejection ABC example
 */
public class NoData {


    public static final int MAX_YEARS_TO_RUN = 50;
    /**
     * what changes
     */
    private static final List<SimpleOptimizationParameter> parameters = new LinkedList<>();


    static {

        //gear for the two boats
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.delegate.averageCatchability",
                                                .0001,0.01)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.delegate.selectivityAParameter",
                                                10,40)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.gear.delegate.selectivityBParameter",
                                                3,15)
        );

        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.gear.averageCatchability",
                                                .0001,0.01)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.gear.selectivityAParameter",
                                                10,40)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.gear.selectivityBParameter",
                                                3,15)
        );

        //max days out!
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$0.departingStrategy.decorated.maxHoursOut",
                                                180*24,240*24)
        );
        parameters.add(
                new SimpleOptimizationParameter("fisherDefinitions$1.departingStrategy.decorated.maxHoursOut",
                                                180*24,240*24)
        );



        // market price
        parameters.add(
                new SimpleOptimizationParameter("market.marketPrice",
                                                30000,60000)
        );

        //recruitment function
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.virginRecruits",
                                                15000000,30000000)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.cumulativePhi",
                                                2,10)
        );
        parameters.add(
                new SimpleOptimizationParameter("biologyInitializer.steepness",
                                                0.8,0.95));

        //new entries
        parameters.add(
                new SimpleOptimizationParameter("plugins$0.profitRatioToEntrantsMultiplier",
                                                2,20)
        );
        parameters.add(
                new SimpleOptimizationParameter("plugins$1.profitRatioToEntrantsMultiplier",
                                                2,20)
        );

    }

}
