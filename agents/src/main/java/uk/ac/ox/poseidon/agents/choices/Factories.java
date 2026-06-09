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

package uk.ac.ox.poseidon.agents.choices;

import sim.util.Int2D;
import uk.ac.ox.poseidon.agents.components.VesselComponentRegister;
import uk.ac.ox.poseidon.agents.vessels.VesselScope;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.paths.GridPathFinder;
import uk.ac.ox.poseidon.geography.ports.PortGrid;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Factories {

    private Factories() {}

    public static <O> AverageOptionValuesFactory<O> averageOptionValues() {
        return new AverageOptionValuesFactory<>();
    }

    public static <O> BestOptionsFromFriendsSupplierFactory<O> bestOptionsFromFriends(
        final int maxNumberOfFriends,
        final Factory<? super VesselScope, ? extends VesselComponentRegister<?
            extends OptionValues<O>>>
            optionValuesRegister
    ) {
        return new BestOptionsFromFriendsSupplierFactory<>(
            maxNumberOfFriends, optionValuesRegister
        );
    }

    public static <O> BestOptionsSupplierFactory<O> bestOptions(
        final Factory<? super VesselScope, ? extends VesselComponentRegister<?
            extends OptionValues<O>>>
            optionValuesRegister
    ) {
        return new BestOptionsSupplierFactory<>(optionValuesRegister);
    }

    public static ConstantDestinationSupplierFactory constantDestination(
        final Factory<? super VesselScope, ? extends ModelGrid> modelGrid,
        final Factory<? super VesselScope, ? extends Coordinate> coordinate
    ) {
        return new ConstantDestinationSupplierFactory(modelGrid, coordinate);
    }

    public static EpsilonGreedyDestinationSupplierFactory epsilonGreedyDestination(
        final double epsilon,
        final Factory<? super VesselScope, ? extends Supplier<Int2D>> explorer,
        final Factory<? super VesselScope, ? extends Supplier<Int2D>> exploiter
    ) {
        return new EpsilonGreedyDestinationSupplierFactory(
            epsilon, explorer, exploiter
        );
    }

    public static <O> ExponentialMovingAverageOptionValuesFactory<O>
    exponentialMovingAverageOptionValues(final double alpha) {
        return new ExponentialMovingAverageOptionValuesFactory<>(alpha);
    }

    public static HomePortDestinationSupplierFactory homePortDestination(
        final Factory<? super VesselScope, ? extends PortGrid> portGrid
    ) {
        return new HomePortDestinationSupplierFactory(portGrid);
    }

    public static <O> ImitatingPickerFactory<O> imitatingPicker(
        final Factory<? super VesselScope, ? extends OptionValues<O>> optionValues,
        final Factory<? super VesselScope, ? extends Predicate<? super O>> optionPredicate,
        final Factory<? super VesselScope, ? extends Supplier<OptionValues<O>>>
            optionValuesSupplier
    ) {
        return new ImitatingPickerFactory<>(
            optionValues, optionPredicate, optionValuesSupplier
        );
    }

    public static NeighbourhoodGridExplorerFactory neighbourhoodGridExplorer(
        final Factory<? super VesselScope, ? extends OptionValues<Int2D>> optionValues,
        final Factory<? super VesselScope, ? extends GridPathFinder> pathFinder,
        final Factory<? super VesselScope, ? extends Predicate<? super Int2D>> cellPredicate,
        final Factory<? super VesselScope, ? extends IntSupplier> neighbourhoodSizeSupplier,
        final Factory<? super VesselScope, ? extends Supplier<Int2D>> fallbackCellPicker
    ) {
        return new NeighbourhoodGridExplorerFactory(
            optionValues, pathFinder, cellPredicate, neighbourhoodSizeSupplier, fallbackCellPicker
        );
    }

    public static RandomGridExplorerFactory randomGridExplorer(
        final Factory<? super VesselScope, ? extends Supplier<? extends List<? extends Int2D>>> cellsSupplier,
        final Factory<? super VesselScope, ? extends Predicate<Int2D>> cellPredicate
    ) {
        return new RandomGridExplorerFactory(cellsSupplier, cellPredicate);
    }
}
