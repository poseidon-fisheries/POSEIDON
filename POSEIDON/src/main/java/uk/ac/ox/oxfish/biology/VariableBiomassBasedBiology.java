/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology;

import static java.util.Arrays.stream;
import static java.util.stream.IntStream.range;

public interface VariableBiomassBasedBiology extends LocalBiology {

    double getCarryingCapacity(Species species);

    void setCarryingCapacity(Species s, double newCarryingCapacity);

    void setCurrentBiomass(Species s, double newCurrentBiomass);

    default double getTotalBiomass() {
        double sum = 0.0;
        for (double v : getCurrentBiomass()) sum += v;
        return sum;
    }

    double[] getCurrentBiomass();

    default boolean isEmpty() { return stream(getCurrentBiomass()).allMatch(b -> b == 0); }

    default boolean isFull() {
        return range(0, getCurrentBiomass().length)
            .allMatch(i -> getCurrentBiomass()[i] == getCarryingCapacity(i));
    }

    double getCarryingCapacity(int index);
}
