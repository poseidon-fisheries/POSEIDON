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

package uk.ac.ox.poseidon.geography.grids;

import sim.util.Int2D;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.allocators.Allocator;
import uk.ac.ox.poseidon.geography.utils.LonLatTable;

import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;

public class Factories {

    private Factories() {
    }

    public static <S extends Scope> CellSetFromGridFileFactory<S> cellSetFromGridFile(
        final Factory<? super S, ? extends Path> path,
        final double includedValue
    ) {
        return new CellSetFromGridFileFactory<>(path, includedValue);
    }

    public static ModelGridFactory modelGrid(
        final double resolutionInDegrees,
        final double westLongitude,
        final double eastLongitude,
        final double southLatitude,
        final double northLatitude
    ) {
        return new ModelGridFactory(resolutionInDegrees, westLongitude, eastLongitude, southLatitude, northLatitude);
    }

    public static <S extends Scope> ModelGridWithActiveCellsFactory<S> modelGridWithActiveCells(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Collection<Int2D>> activeCells
    ) {
        return new ModelGridWithActiveCellsFactory<>(modelGrid, activeCells);
    }

    public static <S extends Scope> ModelGridFromLonLatTableFactory<S> modelGridFromLonLatTable(
        final Factory<? super S, ? extends LonLatTable> lonLatTable,
        final int gridWidthInCells,
        final double mapPaddingInDegrees
    ) {
        return new ModelGridFromLonLatTableFactory<>(lonLatTable, gridWidthInCells, mapPaddingInDegrees);
    }

    public static <S extends Scope> NormalisedDoubleGridFromAllocatorFactory<S> normalisedDoubleGridFromAllocator(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Allocator> allocator,
        final Factory<? super S, ? extends Number> totalValue
    ) {
        return new NormalisedDoubleGridFromAllocatorFactory<>(modelGrid, allocator, totalValue);
    }

    public static <S extends Scope> TimeIndexedNetCdfGridReaderFactory<S> timeIndexedNetCdfGridReader(
        final Factory<? super S, ? extends Path> ncFilePath,
        final String timeDimensionName,
        final String latitudeDimensionName,
        final String longitudeDimensionName
    ) {
        return new TimeIndexedNetCdfGridReaderFactory<>(
            ncFilePath, timeDimensionName, latitudeDimensionName, longitudeDimensionName
        );
    }

    public static <S extends Scope> StaticNetCdfGridReaderFactory<S> staticNetCdfGridReader(
        final Factory<? super S, ? extends Path> ncFilePath,
        final String latitudeDimensionName,
        final String longitudeDimensionName
    ) {
        return new StaticNetCdfGridReaderFactory<>(ncFilePath, latitudeDimensionName, longitudeDimensionName);
    }

    public static <S extends Scope> StaticGridsFromNetCdfFactory<S> staticGridsFromNetCdf(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Supplier<StaticNetCdfGridReader>> staticNetCdfGridReader
    ) {
        return new StaticGridsFromNetCdfFactory<>(modelGrid, staticNetCdfGridReader);
    }

}
