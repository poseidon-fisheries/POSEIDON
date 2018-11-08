/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2018  CoHESyS Lab cohesys.lab@gmail.com
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

package uk.ac.ox.oxfish.biology.boxcars;

import com.beust.jcommander.internal.Lists;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.complicated.FromListMeristics;
import uk.ac.ox.oxfish.biology.complicated.GrowthBinByList;
import uk.ac.ox.oxfish.biology.complicated.Meristics;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.data.collectors.FisherDailyCounter;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.function.Predicate;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.*;

public class SPRAgentTest {

    //numbers from the SPR test


    @Test
    public void computesCorrectly() {

        EquallySpacedBertalanffyFactory factory = new EquallySpacedBertalanffyFactory();
        factory.setAllometricAlpha(new FixedDoubleParameter(0.02));
        factory.setAllometricBeta(new FixedDoubleParameter(2.94));
        factory.setMaxLengthInCm(new FixedDoubleParameter(81));
        factory.setRecruitLengthInCm(new FixedDoubleParameter(0d));
        factory.setkYearlyParameter(new FixedDoubleParameter(0.4946723));
        factory.setNumberOfBins(82);


        GrowthBinByList meristics = factory.apply(mock(FishState.class));

        Species fish = new Species("test",meristics);

        //there are two fishers, but you should only sample fisher 1
        int[] lengthsCaught = new int[]{45,46,47,48,49,50,51,52,53,54,55,56,57,58,59,60,61,62,63,64,65,66,67,68,69,70,71,72,73,75,81};
        int[] correctLandings =new int[]{1,1,3,2,8,15,22,20,38,37,52,61,69,69,67,73,82,66,69,58,38,49,36,20,12,16,7,5,2,1,1};

        double reOrderedLandings[] = new double[82];
        for(int i=0; i<lengthsCaught.length; i++)
            reOrderedLandings[lengthsCaught[i]] = correctLandings[i];

        for(int i=0; i<reOrderedLandings.length; i++)
            reOrderedLandings[i] = reOrderedLandings[i] * fish.getWeight(0,i);
        Fisher fisher1 = mock(Fisher.class,RETURNS_DEEP_STUBS);
       // when(fisher1.getDailyCounter()).thenReturn(mock(FisherDailyCounter.class,RETURNS_DEEP_STUBS));
        when(fisher1.getID()).thenReturn(1);
        FisherDailyCounter dailyCounter = fisher1.getDailyCounter();
        doAnswer(invocation -> {
            int bin = (Integer)invocation.getArguments()[2];
            return reOrderedLandings[bin];
        }).when(dailyCounter).getSpecificLandings(any(Species.class), anyInt(), anyInt());
        //fisher 2 returns garbage
        Fisher fisher2 = mock(Fisher.class,RETURNS_DEEP_STUBS);
        when(fisher2.getID()).thenReturn(2);
        when(fisher2.getDailyCounter().getSpecificLandings(any(Species.class),anyInt(),anyInt())).thenReturn(100d);

        SPRAgent agent = new SPRAgent(
                "testtag",
                fish,
                new Predicate<Fisher>() {
                    @Override
                    public boolean test(Fisher fisher) {
                        return fisher.getID()==1;
                    }
                },
                81,
                0.4946723,
                0.394192,
                100,
                1000,
                5,
                0.02d,
                2.94,
                48
        );
        FishState model = mock(FishState.class,RETURNS_DEEP_STUBS);
        when(model.getFishers()).thenReturn(
                FXCollections.observableList(
                        Lists.newArrayList(
                                fisher1,
                                fisher2
                        )

                )
        );
        agent.start(model);
        agent.observeFishers();
        double spr = agent.computeSPR();
        assertEquals(0.08894,spr,.0001);


    }
}