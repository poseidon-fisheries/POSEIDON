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

package uk.ac.ox.oxfish.model.regs.factory;

import uk.ac.ox.oxfish.model.AdditionalStartable;
import uk.ac.ox.oxfish.model.regs.AllowAllAllocationPolicy;
import uk.ac.ox.oxfish.model.regs.ExogenousPercentagePermitFactory;
import uk.ac.ox.oxfish.model.regs.MaxHoldSizeRandomAllocationPolicy;
import uk.ac.ox.oxfish.model.regs.PermitAllocationPolicy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Constructors;

import java.util.LinkedHashMap;
import java.util.function.Supplier;

public class PermitAllocationPolicies {

    public static final LinkedHashMap<String, Supplier<AlgorithmFactory<? extends PermitAllocationPolicy>>> CONSTRUCTORS;
    public static final LinkedHashMap<Class<? extends AlgorithmFactory>, String> NAMES = new LinkedHashMap<>();


    static {
        NAMES.put(AllowAllAllocationPolicyFactory.class, "No effort limit");
        NAMES.put(MaxHoldSizeRandomAllocationPolicyFactory.class, "Max hold size limit");
        NAMES.put(ExogenousPercentagePermitFactory.class, "Yearly percentage of boats");

        CONSTRUCTORS = Constructors.fromNames(NAMES);

    }


    private PermitAllocationPolicies() {
    }
}