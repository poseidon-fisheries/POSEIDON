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

package uk.ac.ox.oxfish.biology.complicated;

import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.model.FishState;

import java.util.Collection;
import java.util.function.Consumer;

/**
 * abstract class that turns aging process from happening to the entire collection of biologies to happen independently
 * to each local one
 */
public abstract class LocalAgingProcess implements AgingProcess {


    /**
     * as a side-effect ages the local biology according to its rules
     *
     * @param biologies   list of local biologies to age
     * @param species
     * @param model          link to the model
     * @param rounding       whether we expect numbers to be rounded to integers
     * @param daysToSimulate simulation days
     */
    @Override
    public void age(
            Collection<AbundanceLocalBiology> biologies, Species species, FishState model, boolean rounding,
            int daysToSimulate) {
        for (AbundanceLocalBiology abundanceLocalBiology : biologies) {
            ageLocally(abundanceLocalBiology, species, model, rounding, daysToSimulate);
        }
    }


    abstract public void ageLocally(
            AbundanceLocalBiology localBiology, Species species, FishState model, boolean rounding,
            int daysToSimulate);


}
