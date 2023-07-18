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

package uk.ac.ox.oxfish.fisher.strategies.fishing;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Catch;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.model.regs.MultiQuotaRegulation;
import uk.ac.ox.oxfish.model.regs.Regulation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 7/27/17.
 */
public class QuotaLimitDecoratorTest {


    @Test
    public void decoratesCorrectly() throws Exception {

        FishingStrategy stub = mock(FishingStrategy.class);
        when(stub.shouldFish(any(), any(), any(), any())).thenReturn(false);

        QuotaLimitDecorator decorator = new QuotaLimitDecorator(stub);

        //short-circuit to false if the decorated stub always returns false!
        assertFalse(decorator.shouldFish(mock(Fisher.class), new MersenneTwisterFast(),
            mock(FishState.class), mock(TripRecord.class)
        ));

    }

    @Test
    public void anarchyNeverGetsStopped() throws Exception {

        //two species world
        Species species1 = new Species("one");
        Species species2 = new Species("two");
        GlobalBiology biology = new GlobalBiology(species1, species2);
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(biology.getSpecies());


        //you have quotas for 100kg a species
        Regulation regulation = new Anarchy();
        Fisher fisher = mock(Fisher.class);
        when(fisher.getRegulation()).thenReturn(regulation);

        //hold
        Hold hold = new Hold(500, biology);
        hold.load(new Catch(new double[]{300, 200})); //very full load!
        when(fisher.getHold()).thenReturn(hold);


        //decorator! (decorated always returns true so it's not important)
        FishingStrategy stub = mock(FishingStrategy.class);
        when(stub.shouldFish(any(), any(), any(), any())).thenReturn(true);
        QuotaLimitDecorator decorator = new QuotaLimitDecorator(stub);
        assertTrue(decorator.shouldFish(fisher, new MersenneTwisterFast(),
            model, mock(TripRecord.class)
        ));


    }

    @Test
    public void quotaRules() throws Exception {

        //two species world
        Species species1 = new Species("one");
        Species species2 = new Species("two");
        GlobalBiology biology = new GlobalBiology(species1, species2);
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(biology.getSpecies());


        //you have quotas for 100kg a species
        MultiQuotaRegulation regulation = new MultiQuotaRegulation(new double[]{100, 100}, model);
        Fisher fisher = mock(Fisher.class);
        when(fisher.getRegulation()).thenReturn(regulation);

        //hold
        Hold hold = new Hold(500, biology);
        hold.load(new Catch(new double[]{0, 0})); //empty!
        when(fisher.getHold()).thenReturn(hold);

        //decorator! (decorated always returns true so it's not important)
        FishingStrategy stub = mock(FishingStrategy.class);
        when(stub.shouldFish(any(), any(), any(), any())).thenReturn(true);

        //there is nothing in the hold so the decorator should return true
        QuotaLimitDecorator decorator = new QuotaLimitDecorator(stub);
        assertTrue(decorator.shouldFish(fisher, new MersenneTwisterFast(),
            model, mock(TripRecord.class)
        ));

        //load up a bit more but still not enough
        hold.load(new Catch(new double[]{50, 50})); //getting full
        assertTrue(decorator.shouldFish(fisher, new MersenneTwisterFast(),
            model, mock(TripRecord.class)
        ));

        //even still, but having exactly the right amount doesn't stop you from fishing
        hold.load(new Catch(new double[]{50, 0})); //getting full
        assertTrue(decorator.shouldFish(fisher, new MersenneTwisterFast(),
            model, mock(TripRecord.class)
        ));

        //but overflowing in one category does make you want to go back!
        hold.load(new Catch(new double[]{50, 0})); //getting full
        assertFalse(decorator.shouldFish(fisher, new MersenneTwisterFast(),
            model, mock(TripRecord.class)
        ));
    }
}