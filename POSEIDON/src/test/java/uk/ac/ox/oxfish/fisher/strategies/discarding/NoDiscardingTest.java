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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 5/3/17.
 */
public class NoDiscardingTest {


    @Test
    public void testing1() throws Exception {

        Species species1 = new Species("first", StockAssessmentCaliforniaMeristics.FAKE_MERISTICS);
        Species species2 = new Species("second", StockAssessmentCaliforniaMeristics.FAKE_MERISTICS);

        GlobalBiology biology = new GlobalBiology(species1, species2);

        Catch caught = new Catch(species1, 100, biology);
        NoDiscarding noDiscarding = new NoDiscarding();

        Catch retained = noDiscarding.chooseWhatToKeep(
            mock(SeaTile.class),
            mock(Fisher.class),
            caught,
            1,
            mock(Regulation.class),
            mock(FishState.class),
            new MersenneTwisterFast()
        );

        Assertions.assertEquals(retained, caught);
        Assertions.assertEquals(retained.getTotalWeight(), caught.getTotalWeight(), .001);
        Assertions.assertEquals(retained.getWeightCaught(0), caught.getWeightCaught(0), .001);
        Assertions.assertEquals(retained.getWeightCaught(1), caught.getWeightCaught(1), .001);

    }


}
