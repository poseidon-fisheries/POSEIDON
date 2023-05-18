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

package uk.ac.ox.oxfish.fisher.selfanalysis.factory;


import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ObjectiveFunctions {


    /**
     * the list of all registered CONSTRUCTORS
     */
    public static final Map<String, Supplier<AlgorithmFactory<? extends ObjectiveFunction<Fisher>>>> CONSTRUCTORS;

    /**
     * a link to go from class back to the name of the constructor
     */
    public static final Map<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();

    static {
        NAMES.put(CashFlowObjectiveFactory.class, "Cash Flow Objective");
        NAMES.put(HourlyProfitObjectiveFactory.class, "Hourly Profit Objective");
        NAMES.put(TargetSpeciesObjectiveFactory.class, "Target Species Hourly Profit");
        NAMES.put(KnifeEdgePerTripFactory.class, "Hourly Knife-Edge Objective");
        NAMES.put(CutoffPerTripObjectiveFactory.class, "Hourly Cutoff Objective");
        NAMES.put(KnifeEdgeCashflowFactory.class, "Cash Flow Knife-Edge Objective");
        NAMES.put(SimulatedProfitCPUEObjectiveFactory.class, "Simulated Profit Objective");
        CONSTRUCTORS = Constructors.fromNames(NAMES);
    }

    private ObjectiveFunctions() {
    }
}