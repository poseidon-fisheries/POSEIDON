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

package uk.ac.ox.oxfish.fisher.selfanalysis;

import uk.ac.ox.oxfish.fisher.Fisher;

/**
 * A function to judge the "fitness" of ourselves or of others; possibly to drive adaptation
 * Created by carrknight on 8/4/15.
 */
public interface ObjectiveFunction<T> {

    /**
     * compute current fitness of some object
     *
     * @param observer the person who is judging the fitness.
     * @param observed the thing whose fitness we are trying to compute
     * @return a fitness value: the higher the better
     */
    double computeCurrentFitness(Fisher observer, T observed);

}
