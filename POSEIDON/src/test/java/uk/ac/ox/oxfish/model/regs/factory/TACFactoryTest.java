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

package uk.ac.ox.oxfish.model.regs.factory;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.MonoQuotaRegulation;
import uk.ac.ox.poseidon.common.core.parameters.FixedDoubleParameter;

import static org.mockito.Mockito.mock;


public class TACFactoryTest {


    @Test
    public void testTAC() throws Exception {

        TACMonoFactory factory = new TACMonoFactory();
        FishState state = new FishState(System.currentTimeMillis());

        factory.setQuota(new FixedDoubleParameter(200));
        final MonoQuotaRegulation tac1 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        //you can create another one, no problem
        final MonoQuotaRegulation tac2 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 200, .0001);

        //consume a bit of the second, it will affect the first
        tac2.reactToSale(mock(Species.class), mock(Fisher.class), 100, 1234, state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 100, .0001);

        //if I create a third tac, it won't replenish quota remaining
        final MonoQuotaRegulation tac3 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac3.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac3.getQuotaRemaining(0), 100, .0001);

        //if I increase the yearly quota, it will affect everyone but not the remaining quota because it has been consumed already
        factory.setQuota(new FixedDoubleParameter(300));
        final MonoQuotaRegulation tac4 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 300, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 300, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac3.getYearlyQuota(), 300, .0001);
        Assertions.assertEquals(tac3.getQuotaRemaining(0), 100, .0001);

        //in fact they are all the same object
        Assertions.assertEquals(tac1, tac2);
        Assertions.assertEquals(tac1, tac3);
        Assertions.assertEquals(tac1, tac4);


    }
}
