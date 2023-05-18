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

package uk.ac.ox.oxfish.fisher.strategies.discarding;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.StockAssessmentCaliforniaMeristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.junit.Assert.assertEquals;
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

        assertEquals(retained, caught);
        assertEquals(retained.getTotalWeight(), caught.getTotalWeight(), .001);
        assertEquals(retained.getWeightCaught(0), caught.getWeightCaught(0), .001);
        assertEquals(retained.getWeightCaught(1), caught.getWeightCaught(1), .001);

    }


}