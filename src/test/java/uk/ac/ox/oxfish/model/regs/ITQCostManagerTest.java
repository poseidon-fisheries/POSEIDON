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

package uk.ac.ox.oxfish.model.regs;

import com.google.common.collect.Lists;
import org.jfree.util.Log;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.initializer.factory.SplitInitializerFactory;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.selfanalysis.profit.Cost;
import uk.ac.ox.oxfish.geography.mapmakers.SimpleMapInitializerFactory;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.itq.ITQOrderBook;
import uk.ac.ox.oxfish.model.regs.factory.Regulations;
import uk.ac.ox.oxfish.model.scenario.PrototypeScenario;
import uk.ac.ox.oxfish.utility.parameters.FixedDoubleParameter;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by carrknight on 8/12/16.
 */
public class ITQCostManagerTest {


    @Test
    public void factoriesCreateIt() throws Exception {


        List<String> regulations = Lists.newArrayList("Mono-ITQ",
                                                      "Multi-ITQ",
                                                      "Multi-ITQ by List",
                                                      "Partial-ITQ") ;

        for(String regulation : regulations)
            testThatCostManagerHasBeenInitialized(regulation);

    }

    private void testThatCostManagerHasBeenInitialized(String regulationName) {
        Log.info("Testing that " + regulationName + " creates 1 cost manager");
        FishState state = initialize(regulationName);
        int count = 0;
        for(Cost cost : state.getFishers().get(0).getOpportunityCosts())
            if(cost instanceof ITQCostManager)
                count++;

        assertEquals(count,1);
    }

    private FishState initialize(final String regulationName) {
        PrototypeScenario scenario = new PrototypeScenario();
        SimpleMapInitializerFactory mapInitializer = new SimpleMapInitializerFactory();
        mapInitializer.setWidth(new FixedDoubleParameter(15));
        mapInitializer.setHeight(new FixedDoubleParameter(1));
        scenario.setBiologyInitializer(new SplitInitializerFactory());
        scenario.setMapInitializer(mapInitializer);
        scenario.setRegulation(Regulations.CONSTRUCTORS.get(regulationName).get());
        scenario.setFishers(1);

        FishState state = new FishState();
        state.setScenario(scenario);
        state.start();
        state.schedule.step(state);
        return state;
    }


    @Test
    public void cost() throws Exception {
        ITQOrderBook first = mock(ITQOrderBook.class);
        when(first.getLastClosingPrice()).thenReturn(1d);
        ITQOrderBook second = mock(ITQOrderBook.class);
        when(second.getLastClosingPrice()).thenReturn(10d);


        Species firstSpecies = mock(Species.class); when(firstSpecies.getIndex()).thenReturn(0);
        Species secondSpecies = mock(Species.class); when(secondSpecies.getIndex()).thenReturn(1);
        FishState model = mock(FishState.class);
        when(model.getSpecies()).thenReturn(Lists.newArrayList(firstSpecies,secondSpecies));

        ITQCostManager costManager = new ITQCostManager(new Function<Species, ITQOrderBook>() {
            @Override
            public ITQOrderBook apply(Species species) {
                if(species == firstSpecies)
                    return first;
                else {
                    assert species == secondSpecies;
                    return second;
                }
            }
        });

        TripRecord record = mock(TripRecord.class);
        when(record.getSoldCatch()).thenReturn(new double[]{5,3});
        assertEquals(35, costManager.cost(mock(Fisher.class), model, record, -1,1 ), .001);

    }
}