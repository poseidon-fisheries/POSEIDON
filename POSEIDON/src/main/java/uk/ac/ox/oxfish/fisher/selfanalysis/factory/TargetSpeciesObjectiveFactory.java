/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.selfanalysis.factory;

import uk.ac.ox.oxfish.fisher.selfanalysis.TargetSpeciesTripObjective;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

/**
 * Builds target species objective
 * Created by carrknight on 3/24/16.
 */
public class TargetSpeciesObjectiveFactory implements AlgorithmFactory<TargetSpeciesTripObjective> {

    private boolean opportunityCosts = true;


    private int speciesIndex = 0;

    public TargetSpeciesObjectiveFactory() {


    }


    /**
     * Applies this function to the given argument.
     *
     * @param fishState the function argument
     * @return the function result
     */
    @Override
    public TargetSpeciesTripObjective apply(FishState fishState) {
        return new TargetSpeciesTripObjective(
            fishState.getSpecies().get(speciesIndex),
            opportunityCosts
        );
    }

    /**
     * Getter for property 'opportunityCosts'.
     *
     * @return Value for property 'opportunityCosts'.
     */
    public boolean isOpportunityCosts() {
        return opportunityCosts;
    }

    /**
     * Setter for property 'opportunityCosts'.
     *
     * @param opportunityCosts Value to set for property 'opportunityCosts'.
     */
    public void setOpportunityCosts(boolean opportunityCosts) {
        this.opportunityCosts = opportunityCosts;
    }

    /**
     * Getter for property 'speciesIndex'.
     *
     * @return Value for property 'speciesIndex'.
     */
    public int getSpeciesIndex() {
        return speciesIndex;
    }

    /**
     * Setter for property 'speciesIndex'.
     *
     * @param speciesIndex Value to set for property 'speciesIndex'.
     */
    public void setSpeciesIndex(int speciesIndex) {
        this.speciesIndex = speciesIndex;
    }
}
