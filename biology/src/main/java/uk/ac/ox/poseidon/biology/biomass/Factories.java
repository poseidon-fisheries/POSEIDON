/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2026, University of Oxford.
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

package uk.ac.ox.poseidon.biology.biomass;

import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.quantities.KilogramsFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.core.scopes.SimulationScope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;
import uk.ac.ox.poseidon.geography.bathymetry.BathymetricGrid;
import uk.ac.ox.poseidon.geography.grids.DoubleGrid;
import uk.ac.ox.poseidon.geography.grids.DoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.NormalisedDoubleGridFromAllocatorFactory;
import uk.ac.ox.poseidon.geography.predicates.IsActiveWaterCellFactory;

import javax.measure.Quantity;
import javax.measure.quantity.Mass;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static uk.ac.ox.poseidon.core.providers.constant.Factories.constantDouble;
import static uk.ac.ox.poseidon.geography.allocators.Factories.filteredAllocator;
import static uk.ac.ox.poseidon.geography.allocators.Factories.supplierAllocator;
import static uk.ac.ox.poseidon.geography.grids.Factories.timeIndexedNetCdfGridReader;

/**
 * Factories for biomass-based biology: per-species and per-community biomass grids, carrying
 * capacity, growth and diffusion rules and their growers/diffusers, recruitment allocation, and
 * reading/scheduling time-indexed biomass snapshots from NetCDF data.
 */
public class Factories {

    private Factories() {}

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a single-species {@link BiomassGrid}
     * @see BiomassGridFactory
     */
    public static BiomassGridFactory biomassGrid(
        final Factory<? super SimulationScope, ? extends ModelGrid> modelGrid,
        final Factory<? super SimulationScope, ? extends Species> species,
        final Factory<? super SimulationScope, ? extends Allocator> biomassAllocator
    ) {
        return new BiomassGridFactory(modelGrid, species, biomassAllocator);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for one {@link BiomassGrid} per species
     * @see BiomassGridsFactory
     */
    public static BiomassGridsFactory biomassGrids(
        final Factory<? super SimulationScope, ? extends ModelGrid> modelGrid,
        final Factory<? super SimulationScope, ? extends List<? extends Species>> species,
        final Factory<? super SimulationScope, ? extends List<? extends Allocator>> biomassAllocators
    ) {
        return new BiomassGridsFactory(modelGrid, species, biomassAllocators);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a {@link FisheableBiomassGrids} over the
     * resolved per-species grids
     * @see FisheableBiomassGridsFactory
     */
    public static FisheableBiomassGridsFactory fisheableBiomassGrids(
        final Factory<? super SimulationScope, ? extends List<? extends BiomassGrid>> biomassGrids
    ) {
        return new FisheableBiomassGridsFactory(biomassGrids);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a {@link BiomassDiffuser}
     * @see BiomassDiffuserFactory
     */
    public static BiomassDiffuserFactory biomassDiffuser(
        final Factory<? super SimulationScope, ? extends BiomassGrid> biomassGrid,
        final Factory<? super SimulationScope, ? extends CarryingCapacityGrid> carryingCapacityGrid,
        final Factory<? super SimulationScope, ? extends BiomassDiffusionRule> biomassDiffusionRule
    ) {
        return new BiomassDiffuserFactory(biomassGrid, carryingCapacityGrid, biomassDiffusionRule);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link CarryingCapacityGrid} wrapping the
     * resolved {@code grid} directly
     * @see CarryingCapacityGridFactory
     */
    public static <S extends Scope> CarryingCapacityGridFactory<S> carryingCapacityGrid(
        final Factory<? super S, ? extends DoubleGrid> grid
    ) {
        return new CarryingCapacityGridFactory<>(grid);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link CarryingCapacityGrid} giving every
     * active water cell the same carrying capacity
     * @see CarryingCapacityGridFactory
     */
    public static <S extends Scope> CarryingCapacityGridFactory<S> uniformCarryingCapacityGrid(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends Quantity<Mass>> carryingCapacity
    ) {
        return new CarryingCapacityGridFactory<>(
            new DoubleGridFromAllocatorFactory<>(
                modelGrid,
                filteredAllocator(
                    supplierAllocator(
                        constantDouble(new KilogramsFactory<>(carryingCapacity))
                    ),
                    new IsActiveWaterCellFactory<>(bathymetricGrid)
                )
            )
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link CarryingCapacityGrid} spreading
     * {@code totalCarryingCapacity} evenly across active water cells
     * @see CarryingCapacityGridFactory
     */
    public static <S extends Scope> CarryingCapacityGridFactory<S> totalCarryingCapacityGrid(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends BathymetricGrid> bathymetricGrid,
        final Factory<? super S, ? extends Quantity<Mass>> totalCarryingCapacity
    ) {
        return new CarryingCapacityGridFactory<>(
            new NormalisedDoubleGridFromAllocatorFactory<>(
                modelGrid,
                filteredAllocator(
                    supplierAllocator(constantDouble(1.0)),
                    new IsActiveWaterCellFactory<>(bathymetricGrid)
                ),
                new KilogramsFactory<>(totalCarryingCapacity)
            )
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link LogisticGrowthRule}
     * @see LogisticGrowthRuleFactory
     */
    public static LogisticGrowthRuleFactory logisticGrowthRule(
        final double growthRate
    ) {
        return new LogisticGrowthRuleFactory(growthRate);
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a
     * {@link SmoothBiomassDiffusionRule}
     * @see SmoothBiomassDiffusionRuleFactory
     */
    public static SmoothBiomassDiffusionRuleFactory smoothBiomassDiffusionRule(
        final double differentialPercentageToMove,
        final double percentageLimitOnDailyMovement
    ) {
        return new SmoothBiomassDiffusionRuleFactory(
            differentialPercentageToMove,
            percentageLimitOnDailyMovement
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a {@link CommonBiomassGrower}
     * @see CommonBiomassGrowerFactory
     */
    public static CommonBiomassGrowerFactory commonBiomassGrower(
        final Factory<? super SimulationScope, ? extends BiomassGrid> biomassGrid,
        final Factory<? super SimulationScope, ? extends CarryingCapacityGrid> carryingCapacityGrid,
        final Factory<? super SimulationScope, ? extends BiomassGrowthRule> biomassGrowthRule,
        final Factory<? super SimulationScope, ? extends BiomassRecruitmentAllocator>
            biomassRecruitmentAllocator
    ) {
        return new CommonBiomassGrowerFactory(
            biomassGrid,
            carryingCapacityGrid,
            biomassGrowthRule,
            biomassRecruitmentAllocator
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for an {@link IndependentBiomassGrower}
     * @see IndependentBiomassGrowerFactory
     */
    public static IndependentBiomassGrowerFactory independentBiomassGrower(
        final Factory<? super SimulationScope, ? extends BiomassGrid> biomassGrid,
        final Factory<? super SimulationScope, ? extends CarryingCapacityGrid> carryingCapacityGrid,
        final Factory<? super SimulationScope, ? extends BiomassGrowthRule> biomassGrowthRule
    ) {
        return new IndependentBiomassGrowerFactory(
            biomassGrid,
            carryingCapacityGrid,
            biomassGrowthRule
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} for a {@link RandomBiomassRecruitmentAllocator}
     * @see RandomBiomassRecruitmentAllocatorFactory
     */
    public static RandomBiomassRecruitmentAllocatorFactory randomBiomassRecruitmentAllocator() {
        return new RandomBiomassRecruitmentAllocatorFactory();
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for time-indexed, per-species {@link ImmutableBiomassGrid}
     * snapshots read from a NetCDF file, splitting variable names on {@code "_"}
     * @see TimeIndexedBiomassGridsFromNetCdfFactory
     */
    public static <S extends Scope> TimeIndexedBiomassGridsFromNetCdfFactory<S>
        timeIndexedBiomassGridsFromNetCdf(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends List<? extends Species>> species,
        final Factory<? super S, ? extends Path> ncFilePath
    ) {
        return timeIndexedBiomassGridsFromNetCdf(modelGrid, species, ncFilePath, "_");
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for time-indexed, per-species {@link ImmutableBiomassGrid}
     * snapshots read from a NetCDF file, splitting variable names on {@code separator}
     * @see TimeIndexedBiomassGridsFromNetCdfFactory
     */
    public static <S extends Scope> TimeIndexedBiomassGridsFromNetCdfFactory<S>
        timeIndexedBiomassGridsFromNetCdf(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends List<? extends Species>> species,
        final Factory<? super S, ? extends Path> ncFilePath,
        final String separator
    ) {
        return new TimeIndexedBiomassGridsFromNetCdfFactory<>(
            modelGrid, species, separator,
            timeIndexedNetCdfGridReader(ncFilePath, "time", "latitude", "longitude")
        );
    }

    /**
     * @return a {@link uk.ac.ox.poseidon.core.SimulationScopeFactory} scheduling {@code timeIndexedBiomassGrids} to
     * replace {@code biomassGrids}' contents over time
     * @see TimeIndexedBiomassGridUpdatesFactory
     */
    public static TimeIndexedBiomassGridUpdatesFactory timeIndexedBiomassGridUpdates(
        final Factory<? super SimulationScope, ? extends FisheableBiomassGrids> biomassGrids,
        final Factory<? super SimulationScope, ? extends Map<LocalDateTime, ? extends List<? extends SpeciesGrid>>>
            timeIndexedBiomassGrids
    ) {
        return new TimeIndexedBiomassGridUpdatesFactory(biomassGrids, timeIndexedBiomassGrids);
    }
}
