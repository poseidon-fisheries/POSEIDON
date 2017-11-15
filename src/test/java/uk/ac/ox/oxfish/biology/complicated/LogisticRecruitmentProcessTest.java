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

import org.junit.Assert;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;

import static org.mockito.Mockito.mock;

/**
 * Created by carrknight on 7/5/17.
 */
public class LogisticRecruitmentProcessTest {




    private final static Meristics meristics = new FromListMeristics(.99,new Double[]{1d,1d,1d},
                                                                     new double[]{1d, 2d, 3d});


    @Test
    public void logisticGrowth() throws Exception {

        LogisticRecruitmentProcess process = new LogisticRecruitmentProcess(
                400d,.5
        );
        double recruit = process.recruit(mock(Species.class), meristics,
                                         new StructuredAbundance(new double[]{0, 0, 0}, new double[]{12, 4, 60}));

        //recruits ought to be weighin a total of 50kg, so that there ought to be 50 of them
        Assert.assertEquals((int)recruit,50);




    }

    @Test
    public void noRecruitsAboveCarryingCapacity() throws Exception {

        LogisticRecruitmentProcess process = new LogisticRecruitmentProcess(
                201,100
        );
        double recruit = process.recruit(mock(Species.class), meristics,
                                         new StructuredAbundance(
                                      new double[]{0, 0, 0}, new double[]{12, 4, 60}));

        //recruits ought to be weighin a total of 1kg, so that there ought to be 1 of them
        Assert.assertEquals((int)recruit,1);




    }


    @Test
    public void noGrowthFromDepletion() throws Exception {

        LogisticRecruitmentProcess process = new LogisticRecruitmentProcess(
                400d,.5
        );
        double recruit = process.recruit(mock(Species.class), meristics,
                                         new StructuredAbundance(
                                      new double[]{0, 0, 0}, new double[]{0,0, 0}));

        Assert.assertEquals((int)recruit,0);


    }
}