/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2019-2025, University of Oxford.
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

package uk.ac.ox.oxfish.model.regs;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;
import uk.ac.ox.oxfish.model.regs.factory.MaxHoursOutFactory;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OffSwitchDecoratorTest {


    //test it on max hours out
    @Test
    public void hoursOut() {

        MaxHoursOutFactory factory = new MaxHoursOutFactory();
        factory.setMaxHoursOut(new FixedDoubleParameter(100));
        factory.setDelegate(new AnarchyFactory());

        FishState model = mock(FishState.class);
        MaxHoursOutRegulation hoursOut = factory.apply(model);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getHoursAtSeaThisYear()).thenReturn(20d);
        Assertions.assertTrue(hoursOut.allowedAtSea(fisher, model));

        OffSwitchDecorator decorator = new OffSwitchDecorator(hoursOut, true);
        Assertions.assertFalse(decorator.allowedAtSea(fisher, model));
        decorator.setTurnedOff(false);
        Assertions.assertTrue(decorator.allowedAtSea(fisher, model));


        when(fisher.getHoursAtSeaThisYear()).thenReturn(120d);
        Assertions.assertFalse(hoursOut.allowedAtSea(fisher, model));
        Assertions.assertFalse(decorator.allowedAtSea(fisher, model));
        decorator.setTurnedOff(true);
        Assertions.assertFalse(decorator.allowedAtSea(fisher, model));

    }

    @Test
    public void canFishHere() {

        SeaTile tile = mock(SeaTile.class);
        FishState state = mock(FishState.class);
        when(tile.isProtected()).thenReturn(true);

        when(state.getDayOfTheYear()).thenReturn(100);

        TemporaryProtectedArea reg1 = new TemporaryProtectedArea(10, 300);
        TemporaryProtectedArea reg2 = new TemporaryProtectedArea(10, 30);
        TemporaryProtectedArea reg3 = new TemporaryProtectedArea(150, 300);

        Assertions.assertFalse(reg1.canFishHere(
            mock(Fisher.class),
            tile,
            state
        ));
        Assertions.assertTrue(reg2.canFishHere(
            mock(Fisher.class),
            tile,
            state
        ));
        Assertions.assertTrue(reg3.canFishHere(
            mock(Fisher.class),
            tile,
            state
        ));

        OffSwitchDecorator decorator = new OffSwitchDecorator(reg2, true);
        Assertions.assertFalse(decorator.canFishHere(
            mock(Fisher.class),
            tile,
            state
        ));
        decorator.setTurnedOff(false);
        Assertions.assertTrue(decorator.canFishHere(
            mock(Fisher.class),
            tile,
            state
        ));
    }


}
