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

package uk.ac.ox.oxfish.fisher.strategies;

import ec.util.MersenneTwisterFast;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.ac.ox.oxfish.fisher.Fisher;
import uk.ac.ox.oxfish.fisher.actions.AtPort;
import uk.ac.ox.oxfish.fisher.strategies.destination.FavoriteDestinationStrategy;
import uk.ac.ox.oxfish.geography.SeaTile;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FavoriteDestinationStrategyTest {


    //if i am at port, I should be told to move favorite tile
    @Test
    public void depart() throws Exception {
        Fisher fisher = mock(Fisher.class);
        SeaTile portTile = new SeaTile(0, 0, 1, new TileHabitat(0d));
        SeaTile favoriteTile = new SeaTile(1, 1, -1, new TileHabitat(0d));
        Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(portTile);
        when(fisher.getHomePort()).thenReturn(port);

        when(fisher.getDestination()).thenReturn(portTile);
        when(fisher.getLocation()).thenReturn(portTile);
        when(fisher.isAtDestination()).thenReturn(true);

        FavoriteDestinationStrategy strategy = new FavoriteDestinationStrategy(favoriteTile);
        SeaTile destination = strategy.chooseDestination(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(AtPort.class)
        );

        Assertions.assertEquals(destination, favoriteTile);


    }


    //if i am on the move but not there yet, I should be told to keep going
    @Test
    public void keepGoing() throws Exception {
        Fisher fisher = mock(Fisher.class);
        SeaTile portTile = new SeaTile(0, 0, 1, new TileHabitat(0d));
        SeaTile favoriteTile = new SeaTile(1, 1, -1, new TileHabitat(0d));
        SeaTile transitTile = new SeaTile(1, 0, -1, new TileHabitat(0d));
        Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(portTile);
        when(fisher.getHomePort()).thenReturn(port);

        when(fisher.getDestination()).thenReturn(favoriteTile);
        when(fisher.getLocation()).thenReturn(transitTile);
        when(fisher.isAtDestination()).thenReturn(false);

        FavoriteDestinationStrategy strategy = new FavoriteDestinationStrategy(favoriteTile);
        SeaTile destination = strategy.chooseDestination(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(AtPort.class)
        );

        Assertions.assertEquals(destination, favoriteTile);


    }

    //if I arrived I should be told to go back to port
    @Test
    public void goBack() throws Exception {
        Fisher fisher = mock(Fisher.class);
        SeaTile portTile = new SeaTile(0, 0, 1, new TileHabitat(0d));
        SeaTile favoriteTile = new SeaTile(1, 1, -1, new TileHabitat(0d));
        Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(portTile);
        when(fisher.getHomePort()).thenReturn(port);

        when(fisher.getDestination()).thenReturn(favoriteTile);
        when(fisher.getLocation()).thenReturn(favoriteTile);
        when(fisher.isAtDestination()).thenReturn(true);

        FavoriteDestinationStrategy strategy = new FavoriteDestinationStrategy(favoriteTile);
        SeaTile destination = strategy.chooseDestination(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(AtPort.class)
        );
        Assertions.assertEquals(destination, portTile);


    }

    //if I am going home, I keep going home
    @Test
    public void keepGoingBack() throws Exception {
        Fisher fisher = mock(Fisher.class);
        SeaTile portTile = new SeaTile(0, 0, 1, new TileHabitat(0d));
        SeaTile favoriteTile = new SeaTile(1, 1, -1, new TileHabitat(0d));
        SeaTile transitTile = new SeaTile(1, 0, -1, new TileHabitat(0d));

        Port port = mock(Port.class);
        when(port.getLocation()).thenReturn(portTile);
        when(fisher.getHomePort()).thenReturn(port);
        when(fisher.isGoingToPort()).thenReturn(true);


        when(fisher.getLocation()).thenReturn(transitTile);

        FavoriteDestinationStrategy strategy = new FavoriteDestinationStrategy(favoriteTile);
        SeaTile destination = strategy.chooseDestination(fisher,
            new MersenneTwisterFast(), mock(FishState.class),
            mock(AtPort.class)
        );

        Assertions.assertEquals(destination, portTile);


    }

}
