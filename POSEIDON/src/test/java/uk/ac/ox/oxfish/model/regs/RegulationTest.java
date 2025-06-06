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

package uk.ac.ox.oxfish.model.regs;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.Action;
import uk.ac.ox.oxfish.fisher.actions.ActionResult;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.equipment.Boat;
import uk.ac.ox.oxfish.fisher.equipment.Hold;
import uk.ac.ox.oxfish.fisher.equipment.gear.Gear;
import uk.ac.ox.oxfish.fisher.strategies.departing.DepartingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.destination.DestinationStrategy;
import uk.ac.ox.oxfish.fisher.strategies.discarding.DiscardingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.fishing.FishingStrategy;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.fisher.strategies.weather.IgnoreWeatherStrategy;
import uk.ac.ox.oxfish.geography.NauticalMap;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.*;


public class RegulationTest {


    //if the regulations never allow to go out, you don't go out


    @Test
    public void neverGoOut() throws Exception {

        Fisher fisher = mock(Fisher.class);
        Regulation regulation = mock(Regulation.class);
        when(regulation.allowedAtSea(any(), any())).thenReturn(false); //you can't go out!

        AtPort port = new AtPort();
        when(fisher.shouldFisherLeavePort(any())).thenReturn(true); //the fisher wants to go out!
        ActionResult act = port.act(mock(FishState.class), fisher, regulation, 1);

        //must be still at port!
        Assertions.assertTrue(act.getNextState() instanceof AtPort);
        Assertions.assertFalse(act.isActAgainThisTurn());
        //in fact it should have never even checked if I wanted to leave port
        verify(fisher, never()).shouldFisherLeavePort(any());

    }

    //update direction goes back to port if you are not allowed to leave

    @Test
    public void goBackToPort() throws Exception {
        Port port = mock(Port.class);
        SeaTile portTile = mock(SeaTile.class);
        when(port.getLocation()).thenReturn(portTile);
        DestinationStrategy destination = mock(DestinationStrategy.class);
        SeaTile destinationTile = mock(SeaTile.class);


        when(destination.chooseDestination(any(), any(), any(), any())).thenReturn(destinationTile);
        FishState model = mock(FishState.class);
        when(model.getMap()).thenReturn(mock(NauticalMap.class));

        Regulation regs = mock(Regulation.class);

        final Boat mock = mock(Boat.class);
        when(mock.isFuelEnoughForTrip(anyDouble(), anyDouble())).thenReturn(true);
        Fisher fisher = new Fisher(0, port, new MersenneTwisterFast(),
            regs,
            mock(DepartingStrategy.class),
            destination,
            mock(FishingStrategy.class), mock(GearStrategy.class), mock(DiscardingStrategy.class),
            new IgnoreWeatherStrategy(), mock, mock(Hold.class), mock(Gear.class), 0
        );

        when(regs.allowedAtSea(fisher, model)).thenReturn(true);
        fisher.updateDestination(model, mock(Action.class));
        Assertions.assertEquals(fisher.getDestination(), destinationTile);

        //if i don't allow it though it will return to home
        when(regs.allowedAtSea(fisher, model)).thenReturn(false);
        fisher.updateDestination(model, mock(Action.class));
        Assertions.assertEquals(fisher.getDestination(), portTile);


    }
}
