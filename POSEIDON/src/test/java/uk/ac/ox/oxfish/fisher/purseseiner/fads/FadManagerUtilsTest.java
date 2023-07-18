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

import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.gear.FixedProportionGear;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.ac.ox.oxfish.fisher.purseseiner.fads.FadManager.getFadManager;

public class FadManagerUtilsTest {

    @Test
    public void getFadManagerShouldFailForNonPurseSeineGear() {
        final Fisher fisher = mock(Fisher.class);
        final Gear gear = new FixedProportionGear(0);
        when(fisher.getGear()).thenReturn(gear);
        assertThrows(IllegalArgumentException.class, () -> getFadManager(fisher));
    }

}