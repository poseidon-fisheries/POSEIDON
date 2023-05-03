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

package uk.ac.ox.oxfish.biology.complicated;

import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class FixedRecruitmentProcessTest {


    @Test
    public void fixedRecruits() throws Exception {

        FixedRecruitmentProcess fixedRecruitmentProcess = new FixedRecruitmentProcess(365);
        assertEquals(fixedRecruitmentProcess.recruit(mock(Species.class),
                                                     mock(Meristics.class),
                                                     mock(StructuredAbundance.class),
                                                     12,
                                                     365),
                     365d,
                     0.001);

        assertEquals(fixedRecruitmentProcess.recruit(mock(Species.class),
                                                     mock(Meristics.class),
                                                     mock(StructuredAbundance.class),
                                                     12,
                                                     1),
                     1d,
                     0.001);



    }
}