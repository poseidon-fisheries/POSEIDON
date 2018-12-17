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

package uk.ac.ox.oxfish.fisher;

import ec.util.MersenneTwisterFast;
import org.junit.Test;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.equipment.*;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.log.TripRecord;
import uk.ac.ox.oxfish.fisher.strategies.RandomThenBackToPortDestinationStrategyTest;
import uk.ac.ox.oxfish.fisher.strategies.departing.FixedProbabilityDepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.NoDiscarding;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishUntilFullStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.FixedGearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.IgnoreWeatherStrategy;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.FixedPriceMarket;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.regs.factory.AnarchyFactory;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FisherTest {


    @Test
    public void currentTripLoggedCorrectly() throws Exception {

        final int kmPerCell = 1;
        FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimple2x2Map(kmPerCell);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());

        when(fishState.getHoursPerStep()).thenReturn(1d);


        Species species = new Species("pippo");
        GlobalBiology biology = new GlobalBiology(species);
        when(fishState.getBiology()).thenReturn(biology);
        when(fishState.getSpecies()).thenReturn(Collections.singletonList(species));

        double gasCostPerLiter = 10;
        double litersPerKm = 10;
        double kph = 1.0;


        final MarketMap marketMap = new MarketMap(biology);
        final int pricePerFish = 23;
        final FixedPriceMarket fishmarket = new FixedPriceMarket(pricePerFish);
        marketMap.addMarket(species, fishmarket);
        Port port = new Port("Port 0", fishState.getMap().getSeaTile(1, 1), marketMap, gasCostPerLiter);

        Gear gear = mock(Gear.class);
        //catch 1 pound of fish a day
        when(gear.fish(any(), any(),any() , anyInt(), any())).thenReturn(new Catch(species, 1, biology));
        //9 liters each time you fish
        when(gear.getFuelConsumptionPerHourOfFishing(any(),any(),any())).thenReturn(9d);
        Fisher fisher = new Fisher(0, port,
                                   new MersenneTwisterFast(),
                                   new AnarchyFactory().apply(fishState),
                                   new FixedProbabilityDepartingStrategy(1.0, false),
                                   new FavoriteDestinationStrategy(fishState.getMap().getSeaTile(0, 1)),
                                   new FishUntilFullStrategy(1.0),
                                   new FixedGearStrategy(),
                                   new NoDiscarding(),
                                   new IgnoreWeatherStrategy(),
                                   new Boat(1, 1, new Engine(1, litersPerKm, kph), new FuelTank(1000000)),
                                   new Hold(2,biology), gear, 1);
        fisher.start(fishState);
        fishmarket.start(fishState);
        //step it, it should reach the sea tile and do nothing
        fisher.step(fishState);
        assertEquals(fisher.getHoursAtSea(),1,.001);
        TripRecord record = fisher.getCurrentTrip();
        assertEquals(0, fisher.getTotalWeightOfCatchInHold(), .001);

        fisher.step(fishState);
        assertEquals(fisher.getHoursAtSea(), 2, .001);
        fisher.step(fishState);
        assertEquals(fisher.getHoursAtSea(), 3,.001);
        //ready to go home
        assertEquals(2.0, fisher.getTotalWeightOfCatchInHold(), .001);

        //home now
        fisher.step(fishState);
        //don' let you go out again
        fisher.setDepartingStrategy(new FixedProbabilityDepartingStrategy(0, false));
        assertEquals(fisher.getHoursAtSea(), 4, .001);
        fisher.step(fishState);
        assertEquals(fisher.getHoursAtSea(), 0,.001);
        assertEquals(0.0, fisher.getTotalWeightOfCatchInHold(), .001);

        assertEquals(record.isCutShort(), false);
        //2 km, 20 liters of fuel, 10$ each liter
        //2 steps fishing, 9  liters each, 10$ per liter
        //--->
        //380$ of costs per step
        assertEquals(record.getProfitPerHour(false), (23*2 - 10.0 * 10 * 2 - 2 * 10 * 9)/4,.001);


    }


    @Test
    public void fuelEmergencyOverride()
    {
        //exactly like above, except that now the tank is only 30 liters
        final int kmPerCell = 1;
        FishState fishState = RandomThenBackToPortDestinationStrategyTest.generateSimple2x2Map(kmPerCell);
        when(fishState.getRandom()).thenReturn(new MersenneTwisterFast());

        when(fishState.getHoursPerStep()).thenReturn(1d);


        Species species = new Species("pippo");
        GlobalBiology biology = new GlobalBiology(species);
        when(fishState.getBiology()).thenReturn(biology);
        when(fishState.getSpecies()).thenReturn(Collections.singletonList(species));


        double gasCostPerLiter = 10;
        double litersPerKm = 10;
        double kph = 1.0;


        final MarketMap marketMap = new MarketMap(biology);
        final int pricePerFish = 23;
        final FixedPriceMarket fishmarket = new FixedPriceMarket(pricePerFish);
        marketMap.addMarket(species, fishmarket);
        Port port = new Port("Port 0", fishState.getMap().getSeaTile(1, 1), marketMap, gasCostPerLiter);

        Gear gear = mock(Gear.class);
        //catch 1 pound of fish a day
        when(gear.fish(any(), any(),any() , anyInt(), any())).thenReturn(new Catch(species, 1, biology));
        //10 liters each time you fish
        when(gear.getFuelConsumptionPerHourOfFishing(any(),any(),any())).thenReturn(10d);
        Fisher fisher = new Fisher(0, port,
                                   new MersenneTwisterFast(),
                                   new AnarchyFactory().apply(fishState),
                                   new FixedProbabilityDepartingStrategy(1.0, false),
                                   new FavoriteDestinationStrategy(fishState.getMap().getSeaTile(0, 1)),
                                   new FishUntilFullStrategy(1.0),
                                   new FixedGearStrategy(),
                                   new NoDiscarding(),
                                   new IgnoreWeatherStrategy(),
                                   new Boat(1, 1, new Engine(1, litersPerKm, kph), new FuelTank(30)),
                                   new Hold(2,biology),
                                   gear, 1);



        fisher.start(fishState);
        fishmarket.start(fishState);
        //step it, it should reach the sea tile and do nothing
        fisher.step(fishState);
        assertEquals(fisher.getHoursAtSea(), 1,.001);
        assertEquals(fisher.getFuelLeft(), 20.0, 0.0);
        //step it again, it should fish a bit
        fisher.step(fishState);
        assertEquals(1.0, fisher.getTotalWeightOfCatchInHold(), .001);
        assertEquals(fisher.getFuelLeft(), 10.0, 0.0);
        //and now emergency should kick in and you should go back home
        fisher.step(fishState);
        fisher.setDepartingStrategy(new FixedProbabilityDepartingStrategy(0, false));
        assertEquals(fisher.getFuelLeft(), 0.0, 0.0);
        assertTrue(fisher.isFuelEmergencyOverride());
        fisher.step(fishState);
        assertEquals(fisher.getHoursAtSea(), 0,.001);
        assertEquals(fisher.getFuelLeft(), 30.0, 0.0);


    }
}