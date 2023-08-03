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

package uk.ac.ox.oxfish.model.scenario;

import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.regulation.EverythingPermitted;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.regulations.api.Regulation;

import java.time.LocalDate;

/**
 * A scenario is a set of commands called to initialize the model
 */
public interface Scenario {

    /**
     * this is the very first method called by the model when it is started. The scenario needs to instantiate all the
     * essential objects for the model to take place
     *
     * @param model the model
     * @return a scenario-result object containing the map, the list of agents and the biology object
     */
    ScenarioEssentials start(FishState model);


    /**
     * called shortly after the essentials are set, it is time now to return a list of all the agents
     *
     * @param model the model
     * @return a list of agents
     */
    ScenarioPopulation populateModel(FishState model);

    default AlgorithmFactory<? extends Regulation> getRegulations() {
        return new EverythingPermitted();
    }

    default LocalDate getStartDate() {
        return LocalDate.now();
    }

}
