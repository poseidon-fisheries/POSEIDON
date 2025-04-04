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

import eu.project.surimi.Workflow;
import org.joda.money.CurrencyUnit;
import org.joda.money.IllegalCurrencyException;
import org.joda.money.Money;
import uk.ac.ox.poseidon.agents.market.BiomassMarket;
import uk.ac.ox.poseidon.agents.market.BiomassMarketGrid;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Simulation;
import uk.ac.ox.poseidon.geography.grids.ObjectGrid;

import javax.measure.Unit;
import javax.measure.format.MeasurementParseException;
import javax.measure.quantity.Mass;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Set;

import static io.grpc.Status.FAILED_PRECONDITION;
import static io.grpc.Status.INVALID_ARGUMENT;
import static java.lang.System.Logger.Level.INFO;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

public class UpdatePricesRequestHandler extends
    WithSimulationRequestHandler<Workflow.UpdatePricesRequest, Workflow.UpdatePricesResponse> {

    private static final System.Logger logger =
        System.getLogger(UpdatePricesRequestHandler.class.getName());

    public UpdatePricesRequestHandler(final SimulationManager simulationManager) {
        super(simulationManager);
    }

    private static Map<String, Species> getSpeciesByCode(final Simulation simulation) {
        return simulation
            .getComponents(Species.class)
            .stream()
            .collect(toMap(Species::getCode, identity()));
    }

    private static Set<BiomassMarketGrid> getBiomassMarketGrids(final Simulation simulation) {
        final Set<BiomassMarketGrid> marketGrids =
            simulation.getComponents(BiomassMarketGrid.class);
        if (marketGrids.isEmpty()) {
            throw FAILED_PRECONDITION
                .withDescription("No market grids defined in simulation.")
                .asRuntimeException();
        }
        return marketGrids;
    }

    @Override
    protected String getSimulationId(final Workflow.UpdatePricesRequest request) {
        return request.getSimulationId();
    }

    @Override
    protected Workflow.UpdatePricesResponse getResponseWithSimulation(
        final Workflow.UpdatePricesRequest request,
        final Simulation simulation
    ) {
        final Map<String, Species> speciesByCode = getSpeciesByCode(simulation);
        final Map<String, BiomassMarket> marketsById =
            getBiomassMarketGrids(simulation)
                .stream()
                .flatMap(ObjectGrid::stream)
                .collect(toMap(BiomassMarket::getId, identity()));
        request.getPricesList().forEach(price -> {
            final BiomassMarket market = getOrThrow(
                marketsById,
                price.getPortId(),
                "Market"
            );
            final Species species = getOrThrow(
                speciesByCode,
                price.getSpeciesId(),
                "Species"
            );
            final CurrencyUnit currencyUnit = parseCurrency(price.getCurrency());
            final Unit<Mass> biomassUnit = parseMassUnit(price.getMeasurementUnit());
            final BiomassMarket.Price marketPrice =
                new BiomassMarket.Price(
                    Money.of(currencyUnit, price.getPrice(), RoundingMode.HALF_EVEN),
                    biomassUnit
                );
            market.setPrice(species, marketPrice);
            logger.log(
                INFO,
                "Updated price of species {0} at port market {1} to {2}/{3}.",
                species.getCode(),
                market.getId(),
                marketPrice.amount(),
                marketPrice.biomassUnit()
            );
        });
        return Workflow.UpdatePricesResponse.newBuilder().build();
    }

    private CurrencyUnit parseCurrency(final String currency) {
        try {
            return CurrencyUnit.of(currency);
        } catch (final IllegalCurrencyException e) {
            throw wrap(INVALID_ARGUMENT, e);
        }
    }

    private Unit<Mass> parseMassUnit(final String massUnit) {
        try {
            return BiomassMarket.parseMassUnit(massUnit);
        } catch (final MeasurementParseException e) {
            throw wrap(INVALID_ARGUMENT, e);
        }
    }

}
