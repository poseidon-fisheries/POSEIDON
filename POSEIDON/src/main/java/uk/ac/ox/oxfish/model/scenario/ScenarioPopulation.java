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

import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.network.SocialNetwork;

import java.util.List;
import java.util.Map;

/**
 * The result of calling populateModel from the scenario
 * Created by carrknight on 7/1/15.
 */
public class ScenarioPopulation
{


    /**
     * the initial list of fishers
     */
    private final List<Fisher> population;

    /**
     * the social network describing how the fishers are connected: should NOT be populated
     */
    private final SocialNetwork network;

    /**
     * method to create and kill fishers while the model is running; mapping name of the population to fishery factory
     */
    private final Map<String,FisherFactory> factory;

    /**
     * The list of agents and a network ready to be populated!
     * @param population list of agents
     * @param network   network NOT populated
     * @param factory
     */
    public ScenarioPopulation(List<Fisher> population, SocialNetwork network,
                              Map<String,FisherFactory> factory) {
        this.population = population;
        this.network = network;
        this.factory = factory;
    }


    public List<Fisher> getPopulation() {
        return population;
    }

    public SocialNetwork getNetwork() {
        return network;
    }

    public Map<String,FisherFactory> getFactory() {
        return factory;
    }
}
