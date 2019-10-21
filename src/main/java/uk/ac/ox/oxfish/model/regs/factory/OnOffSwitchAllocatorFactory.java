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

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.OnOffSwitchRegulator;
import uk.ac.ox.oxfish.model.regs.PermitAllocationPolicy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OnOffSwitchAllocatorFactory implements AlgorithmFactory<OnOffSwitchRegulator>{


    /**
     * as long as a fisher has one of these tags, it will be part of the fishery
     */
    private String tagsOfParticipants = "population0,population1";

    private AlgorithmFactory<? extends PermitAllocationPolicy> permitPolicy = new AllowAllAllocationPolicyFactory();


    /**
     * Applies this function to the given argument.
     *
     * @param state the function argument
     * @return the function result
     */
    @Override
    public OnOffSwitchRegulator apply(FishState state) {




        return new OnOffSwitchRegulator(
                permitPolicy.apply(state),
                Arrays.asList(this.tagsOfParticipants.split(",")).
                        stream().map(
                        String::trim
                ).collect(Collectors.toList())
        );

    }

    /**
     * Getter for property 'tagsOfParticipants'.
     *
     * @return Value for property 'tagsOfParticipants'.
     */
    public String getTagsOfParticipants() {
        return tagsOfParticipants;
    }

    /**
     * Setter for property 'tagsOfParticipants'.
     *
     * @param tagsOfParticipants Value to set for property 'tagsOfParticipants'.
     */
    public void setTagsOfParticipants(String tagsOfParticipants) {
        this.tagsOfParticipants = tagsOfParticipants;
    }

    /**
     * Getter for property 'permitPolicy'.
     *
     * @return Value for property 'permitPolicy'.
     */
    public AlgorithmFactory<? extends PermitAllocationPolicy> getPermitPolicy() {
        return permitPolicy;
    }

    /**
     * Setter for property 'permitPolicy'.
     *
     * @param permitPolicy Value to set for property 'permitPolicy'.
     */
    public void setPermitPolicy(
            AlgorithmFactory<? extends PermitAllocationPolicy> permitPolicy) {
        this.permitPolicy = permitPolicy;
    }
}
