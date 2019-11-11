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

package uk.ac.ox.oxfish.fisher.strategies.destination;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.selfanalysis.ObjectiveFunction;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.factory.RandomThenBackToPortFactory;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.WeatherEmergencyStrategy;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.regs.Anarchy;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.fxcollections.ObservableList;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 2/28/17.
 */
public class StrategyReplicatorTest {


    @Test
    public void oneWins() throws Exception {


        FishState state = mock(FishState.class);
        ObservableList<Fisher> fishers = ObservableList.observableList(new ArrayList<Fisher>());
        when(state.getFishers()).thenReturn(fishers);
        MersenneTwisterFast random = new MersenneTwisterFast();
        when(state.getRandom()).thenReturn(random);
        
        
        //create 100 fishers
        for(int i=0; i < 100; i++) {
            Fisher f = new Fisher(
                    i,
                    mock(Port.class),
                    state.getRandom(),
                    new Anarchy(),
                    mock(DepartingStrategy.class),
                    mock(DestinationStrategy.class),
                    mock(FishingStrategy.class),
                    mock(GearStrategy.class),
                    mock(DiscardingStrategy.class),
                    mock(WeatherEmergencyStrategy.class),
                    mock(Boat.class),
                    mock(Hold.class), mock(Gear.class),
                    1);
            f.start(state);
            fishers.add(f);

        }

        //two factories. The actual strategy they dispense is not important since the utility is going to be a function of their index
        List<AlgorithmFactory<? extends DestinationStrategy>> options = new LinkedList<>();
        options.add(new RandomThenBackToPortFactory());
        options.add(new RandomThenBackToPortFactory());
        //utility is index * 10
        ObjectiveFunction<Fisher> objectiveFunction = (observer, observed) ->
                (((ReplicatorDrivenDestinationStrategy) observed.getDestinationStrategy()).getStrategyIndex()+1) * 10;
        //give random utility
        for(Fisher fisher : fishers)
            fisher.setDestinationStrategy(new ReplicatorDrivenDestinationStrategy(random.nextInt(2),mock(DestinationStrategy.class)));

        StrategyReplicator replicator = new StrategyReplicator(options, objectiveFunction, .2);

        int strategy2Users = 0;
        for(Fisher fisher : fishers)
            if(((ReplicatorDrivenDestinationStrategy) fisher.getDestinationStrategy()).getStrategyIndex()==1)
                strategy2Users++;
        System.out.println(strategy2Users);

        for(int i=0; i<10; i++)
        {
            strategy2Users = 0;
            replicator.step(state);
            for(Fisher fisher : fishers)
                if(((ReplicatorDrivenDestinationStrategy) fisher.getDestinationStrategy()).getStrategyIndex()==1)
                    strategy2Users++;
            System.out.println(strategy2Users);

        }
        //this becomes 0 if everbody switches
        //assertEquals(replicator.getLastObservedFitnesses()[0],10,.001);
        assertEquals(replicator.getLastObservedFitnesses()[1],20,.001);
        assertTrue(strategy2Users > 90);



    }
}