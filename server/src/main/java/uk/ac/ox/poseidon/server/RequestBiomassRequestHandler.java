/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025 CoHESyS Lab cohesys.lab@gmail.com
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

import eu.project.surimi.Biomass;
import eu.project.surimi.Workflow;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;

import java.util.NoSuchElementException;

import static io.grpc.Status.FAILED_PRECONDITION;
import static io.grpc.Status.NOT_FOUND;
import static tech.units.indriya.unit.Units.KILOGRAM;

public class RequestBiomassRequestHandler extends
    WithSimulationRequestHandler<Workflow.RequestBiomassRequest, Workflow.RequestBiomassResponse> {
    public RequestBiomassRequestHandler(final SimulationManager simulationManager) {
        super(simulationManager);
    }

    @Override
    protected String getSimulationId(final Workflow.RequestBiomassRequest request) {
        return request.getSimulationId();
    }

    private BathymetricGrid getBathymetricGrid(final Simulation simulation) {
        try {
            return simulation.getComponent(BathymetricGrid.class);
        } catch (final NoSuchElementException e) {
            throw wrap(NOT_FOUND, e);
        } catch (final IllegalStateException e) {
            throw wrap(FAILED_PRECONDITION, e);
        }
    }

    @Override
    protected Workflow.RequestBiomassResponse getResponseWithSimulation(
        final Workflow.RequestBiomassRequest request,
        final Simulation simulation
    ) {
        final BathymetricGrid bathymetricGrid = getBathymetricGrid(simulation);
        final Workflow.RequestBiomassResponse.Builder responseBuilder =
            Workflow.RequestBiomassResponse
                .newBuilder()
                .setMeasurementUnit(KILOGRAM.getSymbol());
        simulation.getComponents(BiomassGrid.class).forEach(grid -> {
            final Biomass.BiomassGrid.Builder gridBuilder =
                Biomass.BiomassGrid
                    .newBuilder()
                    .setSpeciesId(grid.getSpecies().getCode());
            bathymetricGrid.getActiveWaterCells().forEach(cell -> {
                final Coordinate coordinate =
                    bathymetricGrid.getModelGrid().toCoordinate(cell);
                gridBuilder.addBiomassCells(
                    Biomass.BiomassCell
                        .newBuilder()
                        .setLongitude(coordinate.getLon())
                        .setLatitude(coordinate.getLat())
                        .setBiomass(grid.getDouble(cell))
                        .build()
                );
            });
            responseBuilder.addBiomassGrids(gridBuilder.build());
        });
        return responseBuilder.build();
    }
}
