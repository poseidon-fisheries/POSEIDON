/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2024 CoHESyS Lab cohesys.lab@gmail.com
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
 *
 */

package uk.ac.ox.poseidon.server;

import fishing.BiomassGridServiceGrpc;
import fishing.Fishing;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.io.tables.SimulationEventListenerFactory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExternalBiomassGridProcessFactory
    extends SimulationEventListenerFactory<ExternalBiomassGridProcess> {

    private String serverName;
    private int serverPort;
    private Factory<? extends BiomassGrid> internalBiomassGrid;
    private double carryingCapacity;
    private double growthRate;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    protected ExternalBiomassGridProcess newListener(final Simulation simulation) {

        final BiomassGrid internalGrid = internalBiomassGrid.get(simulation);
        final ModelGrid modelGrid = internalGrid.getModelGrid();
        final var gridBuilder = Fishing.Grid.newBuilder();
        for (int x = 0; x < modelGrid.getGridWidth(); x++) {
            final var columnBuilder = Fishing.Grid.Column.newBuilder();
            for (int y = 0; y < modelGrid.getGridHeight(); y++) {
                columnBuilder.addValues(internalGrid.getValue(new Int2D(x, y)));
            }
            gridBuilder.addColumns(columnBuilder);
        }

        final ManagedChannel channel = ManagedChannelBuilder
            .forAddress(serverName, serverPort)
            .usePlaintext()
            .build();

        simulation.addFinalProcess(channel::shutdown);

        final BiomassGridServiceGrpc.BiomassGridServiceBlockingStub stub =
            BiomassGridServiceGrpc.newBlockingStub(
                channel
            );

        stub.init(
            Fishing.GridDef
                .newBuilder()
                .setGrid(gridBuilder)
                .setCarryingCapacity(carryingCapacity)
                .setGrowthRate(growthRate)
                .build()
        );

        return new ExternalBiomassGridProcess(internalGrid, stub);
    }
}
