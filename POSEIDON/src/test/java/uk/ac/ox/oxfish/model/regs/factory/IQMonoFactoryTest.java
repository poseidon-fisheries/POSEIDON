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


public class IQMonoFactoryTest {
    @Test
    public void testITQ() throws Exception {

        //similar to the TAC test, but the results are different


        IQMonoFactory factory = new IQMonoFactory();
        FishState state = new FishState(System.currentTimeMillis());

        factory.setIndividualQuota(new FixedDoubleParameter(200));
        final MonoQuotaRegulation tac1 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        //create a second one, same parameters but they aren't the same object
        final MonoQuotaRegulation tac2 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 200, .0001);
        Assertions.assertNotEquals(tac1, tac2);

        //consume a bit of the second, it will NOT affect the first
        tac2.reactToSale(mock(Species.class), mock(Fisher.class), 100, 1234, state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 100, .0001);

        //if I create a third tac, it will still be full and the second one will not replenish
        final MonoQuotaRegulation tac3 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac3.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac3.getQuotaRemaining(0), 200, .0001);

        //if I increase the yearly quota, it will affect only the new one
        factory.setIndividualQuota(new FixedDoubleParameter(300));
        final MonoQuotaRegulation tac4 = factory.apply(state);
        Assertions.assertEquals(tac1.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac1.getQuotaRemaining(0), 200, .0001);
        Assertions.assertEquals(tac2.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac2.getQuotaRemaining(0), 100, .0001);
        Assertions.assertEquals(tac3.getYearlyQuota(), 200, .0001);
        Assertions.assertEquals(tac3.getQuotaRemaining(0), 200, .0001);
        Assertions.assertEquals(tac4.getYearlyQuota(), 300, .0001);
        Assertions.assertEquals(tac4.getQuotaRemaining(0), 300, .0001);


    }
}
