/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.biology.tuna;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AbundanceAggregationProcessTest {

    @Test
    public void AbundanceAggregationProcessTester() {
        //final BiologicalProcess<AbundanceLocalBiology> aggregationProcess = new AbundanceAggregationProcess();
        final AbundanceAggregator aggregator = new AbundanceAggregator();
        final Species species1 = new Species("Piano Tuna");
        final GlobalBiology globalBiology = new GlobalBiology(species1);

        final List<AbundanceLocalBiology> localBiologies;
        localBiologies = new ArrayList<>();

        final HashMap<Species, double[][]> abundance = new HashMap<>();
        abundance.put(species1, new double[][]{{10, 5, 1}, {1, 2, 3}});
        localBiologies.add(new AbundanceLocalBiology(abundance));

        abundance.put(species1, new double[][]{{20, 30, 40}, {0, 0, 1}});
        localBiologies.add(new AbundanceLocalBiology(abundance));


        //  final FishState fishState = mock(FishState.class);


        Assertions.assertEquals(30,
            aggregator.apply(globalBiology, localBiologies).getAbundance(species1).asMatrix()[0][0],
            0);
        Assertions.assertEquals(1,
            aggregator.apply(globalBiology, localBiologies).getAbundance(species1).asMatrix()[1][0],
            0);
    }
}
