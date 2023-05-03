/*
 *  POSEIDON, an agent-based model of fisheries
 *  Copyright (C) 2020  CoHESyS Lab cohesys.lab@gmail.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package uk.ac.ox.oxfish.fisher.purseseiner.fads;

import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertEquals;
import static uk.ac.ox.oxfish.biology.GlobalBiology.genericListOfSpecies;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.EPSILON;

public class BiomassLostEventTest {

    @Test
    public void getBiomassLost() {
        final Species species = genericListOfSpecies(1).getSpecie(0);
        final BiomassLostEvent biomassLostEvent =
            new BiomassLostEvent(ImmutableMap.of(species, 1.0));
        assertEquals(1.0, biomassLostEvent.getBiomassLost().get(species), EPSILON);
    }

}