/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
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

package uk.ac.ox.oxfish.geography.mapmakers;

import com.google.common.base.Preconditions;
import ec.util.MersenneTwisterFast;
import uk.ac.ox.oxfish.biology.EmptyLocalBiology;
import uk.ac.ox.oxfish.biology.GlobalBiology;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.geography.*;
import uk.ac.ox.oxfish.geography.habitat.TileHabitat;
import uk.ac.ox.oxfish.geography.ports.Port;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.market.MarketMap;
import uk.ac.ox.oxfish.model.market.MarketProxy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class MapWithFarOffPortsInitializer implements MapInitializer {


    private final MapInitializer delegate;

    private final List<FarOffPortInformation> farOffPorts;


    public MapWithFarOffPortsInitializer(
        MapInitializer delegate,
        List<FarOffPortInformation> farOffPorts
    ) {
        this.delegate = delegate;
        this.farOffPorts = farOffPorts;
    }

    @Override
    public NauticalMap makeMap(MersenneTwisterFast random, GlobalBiology biology, FishState model) {

        final NauticalMap originalMap = delegate.makeMap(random, biology, model);
        final HashMap<SeaTile, FarOffPort> instantiatedPorts = new LinkedHashMap<>();
        //we'll make a copy of this adding ports info
        for (int i = 0; i < farOffPorts.size(); i++) {
            //fake land, immediately
            SeaTile tile = new SeaTile(
                NauticalMapWithFarOffPorts.GRID_X_ALLOCATED_TO_FAR_OFF_PORTS, i,
                1000, new TileHabitat(0)
            );

            //create  market map
            MarketMap marketMap = new MarketMap(biology);
            //because the model is not completed yet in initializing
            //we will have to pass it a proxy.
            //this might be a problem for constructors that expect an ordered call
            final FarOffPortInformation information = farOffPorts.get(i);
            for (Species species : biology.getSpecies())
                marketMap.addMarket(
                    species,
                    new MarketProxy(information.getMarketMaker())
                );


            tile.setBiology(new EmptyLocalBiology());
            Port newPort = new Port(
                information.getPortName(),
                tile,
                marketMap,
                information.getGasPriceAtPort()
            );

            final SeaTile exitTile = originalMap.
                getSeaTile(
                    information.getExitGridX(),
                    information.getExitGridY()
                );

            Preconditions.checkNotNull(exitTile, "Exit tile does not exist for port " + information.getPortName());
            Preconditions.checkArgument(
                exitTile.isWater(),
                "Exit tile is not sea tile for " + information.getPortName()
            );
            FarOffPort farOffPort = new FarOffPort(
                newPort,
                tile,
                exitTile,
                information.getDistanceFromExitInKm(),
                null
            );

            instantiatedPorts.put(tile, farOffPort);


        }


        return new NauticalMapWithFarOffPorts(
            originalMap.getRasterBathymetry(),
            originalMap.getMpaVectorField(),
            originalMap.getDistance(),
            originalMap.getPathfinder(),
            instantiatedPorts


        );
    }
}
