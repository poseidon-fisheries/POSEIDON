/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.actions;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.BiomassLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.fisher.equipment.Catch;

import java.util.Map.Entry;

import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class BiomassCatchMakerTest {

    private final GlobalBiology globalBiology = GlobalBiology.genericListOfSpecies(2);

    private final CatchMaker<BiomassLocalBiology> catchMaker = new BiomassCatchMaker(globalBiology);

    @Test
    public void testApply() {
        final Entry<Catch, BiomassLocalBiology> caughtAndUncaught =
            catchMaker.apply(
                new BiomassLocalBiology(new double[]{10.0, 10.0}),
                new BiomassLocalBiology(new double[]{5.0, 15.0})
            );
        Assertions.assertArrayEquals(new double[]{5.0, 10.0}, caughtAndUncaught.getKey().getBiomassArray(), EPSILON);
        Assertions.assertArrayEquals(new double[]{0.0, 5.0}, caughtAndUncaught.getValue().getCurrentBiomass(), EPSILON);
    }
}