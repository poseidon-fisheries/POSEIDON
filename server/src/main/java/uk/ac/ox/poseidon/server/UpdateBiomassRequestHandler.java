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
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.biomass.BiomassGrid;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.core.utils.Measurements;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Mass;
import java.util.Map;
import java.util.NoSuchElementException;

import static io.grpc.Status.*;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;
import static tech.units.indriya.unit.Units.KILOGRAM;

public class UpdateBiomassRequestHandler extends
    WithSimulationRequestHandler<Workflow.UpdateBiomassRequest, Workflow.UpdateBiomassResponse> {

    public UpdateBiomassRequestHandler(final SimulationManager simulationManager) {
        super(simulationManager);
    }

    private static Int2D getSimulationCell(
        final Biomass.BiomassCell biomassCell,
        final BathymetricGrid bathymetricGrid
    ) {
        final Int2D cell = bathymetricGrid.getModelGrid().toCell(new Coordinate(
            biomassCell.getLongitude(),
            biomassCell.getLatitude()
        ));
        if (!bathymetricGrid.isActiveWater(cell)) {
            throw INVALID_ARGUMENT
                .withDescription(
                    "Coordinates (" +
                        biomassCell.getLongitude() +
                        ", " +
                        biomassCell.getLatitude() +
                        ") do not point to an active water cell.")
                .asRuntimeException();
        }
        return cell;
    }

    @Override
    protected String getSimulationId(final Workflow.UpdateBiomassRequest request) {
        return request.getSimulationId();
    }

    @Override
    protected Workflow.UpdateBiomassResponse getResponseWithSimulation(
        final Workflow.UpdateBiomassRequest request,
        final Simulation simulation
    ) {
        final Unit<Mass> massUnit = parseMassUnit(request.getMeasurementUnit());
        final boolean isKg = massUnit.isEquivalentTo(KILOGRAM);
        final Map<String, BiomassGrid> simulationGrids =
            simulation.getComponents(BiomassGrid.class).stream().collect(toMap(
                biomassGrid -> biomassGrid.getSpecies().getCode(),
                identity()
            ));
        final BathymetricGrid bathymetricGrid = getBathymetricGrid(simulation);
        request.getBiomassGridsList().forEach(biomassGrid -> {
            final BiomassGrid simulationGrid = getOrThrow(
                simulationGrids,
                biomassGrid.getSpeciesId(), "Biomass grid"
            );
            biomassGrid.getBiomassCellsList().forEach(biomassCell -> {
                final Int2D cell = getSimulationCell(biomassCell, bathymetricGrid);
                if (isKg)
                    simulationGrid.setBiomass(cell, biomassCell.getBiomass());
                else
                    simulationGrid.setBiomass(
                        cell,
                        new uk.ac.ox.poseidon.biology.biomass.Biomass(
                            biomassCell.getBiomass(),
                            massUnit
                        )
                    );
            });
        });
        return Workflow.UpdateBiomassResponse.newBuilder().build();
    }

    private Unit<Mass> parseMassUnit(final String massUnit) {
        try {
            return Measurements.parseMassUnit(massUnit);
        } catch (final MeasurementParseException e) {
            throw wrap(INVALID_ARGUMENT, e);
        }
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
}
