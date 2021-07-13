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

package uk.ac.ox.oxfish.fisher.equipment.gear.components;

import uk.ac.ox.oxfish.biology.Species;

/**
 * A subcomponent of the catchability-selectivity-retention gear.
 * This is any filter that takes an array of fish and returns a second array containing
 * all the ones that are selected/caught/etc.
 * Created by carrknight on 3/9/16.
 */
public interface AbundanceFilter {


    /**
     * returns a int[subdivisions][bins] array with male and female fish that are not filtered out
     * @param species the species of fish
     * @param abundance
     * @return an int[subdivisions][bins] array for all the stuff that is caught/selected and so on
     */
    double[][] filter(Species species, double[][] abundance);



}
