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

/**
 * Factories for {@link ModelGrid}s and the {@link DoubleGrid}/NetCDF-reading components built on
 * top of them: building a grid from an explicit bounding box, a table's coordinates, or a raster
 * file's own metadata; narrowing a grid's active cells; building an allocator-driven double grid
 * (fixed or mutable, normalised or not); and reading raster/NetCDF data files.
 */
public class Factories {

    private Factories() {
    }

    /**
     * @param path          factory for the raster grid file to read
     * @param includedValue the cell value to select
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the set of cells in the
     * resolved file whose value equals {@code includedValue}
     * @see CellSetFromGridFileFactory
     */
    public static <S extends Scope> CellSetFromGridFileFactory<S> cellSetFromGridFile(
        final Factory<? super S, ? extends Path> path,
        final double includedValue
    ) {
        return new CellSetFromGridFileFactory<>(path, includedValue);
    }

    /**
     * @param resolutionInDegrees the size of one cell, in degrees; the bounding box's width and
     *                            height must each be an exact multiple of this
     * @param westLongitude       the bounding box's western edge
     * @param eastLongitude       the bounding box's eastern edge
     * @param southLatitude       the bounding box's southern edge
     * @param northLatitude       the bounding box's northern edge
     * @return a {@link uk.ac.ox.poseidon.core.GlobalScopeFactory} for a {@link ModelGrid}
     * @see ModelGridFactory
     */
    public static ModelGridFactory modelGrid(
        final double resolutionInDegrees,
        final double westLongitude,
        final double eastLongitude,
        final double southLatitude,
        final double northLatitude
    ) {
        return new ModelGridFactory(resolutionInDegrees, westLongitude, eastLongitude, southLatitude, northLatitude);
    }

    /**
     * @param modelGrid   factory for the grid to narrow
     * @param activeCells factory for the cells that should be active in the resolved grid
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for the resolved grid,
     * narrowed to {@code activeCells}
     * @see ModelGridWithActiveCellsFactory
     */
    public static <S extends Scope> ModelGridWithActiveCellsFactory<S> modelGridWithActiveCells(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Collection<Int2D>> activeCells
    ) {
        return new ModelGridWithActiveCellsFactory<>(modelGrid, activeCells);
    }

    /**
     * @param lonLatTable         factory for the table whose coordinates the grid should fit
     * @param gridWidthInCells    the width of the resulting grid, in cells
     * @param mapPaddingInDegrees the margin added around the coordinates' bounding box, on every
     *                            side
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link ModelGrid} sized
     * to fit the resolved table's coordinates
     * @see ModelGridFromLonLatTableFactory
     */
    public static <S extends Scope> ModelGridFromLonLatTableFactory<S> modelGridFromLonLatTable(
        final Factory<? super S, ? extends LonLatTable> lonLatTable,
        final int gridWidthInCells,
        final double mapPaddingInDegrees
    ) {
        return new ModelGridFromLonLatTableFactory<>(lonLatTable, gridWidthInCells, mapPaddingInDegrees);
    }

    /**
     * @param modelGrid factory for the grid to build over
     * @param allocator factory for the allocator assigning each cell's value
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an immutable
     * {@link BaseDoubleGrid}
     * @see DoubleGridFromAllocatorFactory
     */
    public static <S extends Scope> DoubleGridFromAllocatorFactory<S> doubleGridFromAllocator(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Allocator> allocator
    ) {
        return new DoubleGridFromAllocatorFactory<>(modelGrid, allocator);
    }

    /**
     * @param modelGrid  factory for the grid to build over
     * @param allocator  factory for the allocator assigning each cell's raw value
     * @param totalValue factory for the value the grid's values should sum to once normalised
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for an immutable, normalised
     * {@link BaseDoubleGrid}
     * @see NormalisedDoubleGridFromAllocatorFactory
     */
    public static <S extends Scope> NormalisedDoubleGridFromAllocatorFactory<S> normalisedDoubleGridFromAllocator(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Allocator> allocator,
        final Factory<? super S, ? extends Number> totalValue
    ) {
        return new NormalisedDoubleGridFromAllocatorFactory<>(modelGrid, allocator, totalValue);
    }

    /**
     * @param modelGrid factory for the grid to build over
     * @param allocator factory for the allocator assigning each cell's value
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a
     * {@link MutableDoubleGrid}
     * @see MutableDoubleGridFromAllocatorFactory
     */
    public static <S extends Scope> MutableDoubleGridFromAllocatorFactory<S> mutableDoubleGridFromAllocator(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Allocator> allocator
    ) {
        return new MutableDoubleGridFromAllocatorFactory<>(modelGrid, allocator);
    }

    /**
     * @param modelGrid  factory for the grid to build over
     * @param allocator  factory for the allocator assigning each cell's raw value
     * @param totalValue factory for the value the grid's values should sum to once normalised
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a normalised
     * {@link MutableDoubleGrid}
     * @see NormalisedMutableDoubleGridFromAllocatorFactory
     */
    public static <S extends Scope> NormalisedMutableDoubleGridFromAllocatorFactory<S>
    normalisedMutableDoubleGridFromAllocator(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Allocator> allocator,
        final Factory<? super S, ? extends Number> totalValue
    ) {
        return new NormalisedMutableDoubleGridFromAllocatorFactory<>(modelGrid, allocator, totalValue);
    }

    /**
     * @param gridFilePath factory for the raster grid file to read
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a {@link ModelGrid} sized
     * to the resolved file's own extent and resolution
     * @see ModelGridFromGridFileFactory
     */
    public static <S extends Scope> ModelGridFromGridFileFactory<S> modelGridFromGridFile(
        final Factory<? super S, ? extends Path> gridFilePath
    ) {
        return new ModelGridFromGridFileFactory<>(gridFilePath);
    }

    /**
     * @param ncFilePath              factory for the NetCDF file to read
     * @param timeDimensionName       the name of the time dimension/coordinate variable
     * @param latitudeDimensionName   the name of the latitude dimension/coordinate variable
     * @param longitudeDimensionName  the name of the longitude dimension/coordinate variable
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a supplier of fresh
     * {@link TimeIndexedNetCdfGridReader}s
     * @see TimeIndexedNetCdfGridReaderFactory
     */
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

    /**
     * @param ncFilePath             factory for the NetCDF file to read
     * @param latitudeDimensionName  the name of the latitude dimension/coordinate variable
     * @param longitudeDimensionName the name of the longitude dimension/coordinate variable
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a supplier of fresh
     * {@link StaticNetCdfGridReader}s
     * @see StaticNetCdfGridReaderFactory
     */
    public static <S extends Scope> StaticNetCdfGridReaderFactory<S> staticNetCdfGridReader(
        final Factory<? super S, ? extends Path> ncFilePath,
        final String latitudeDimensionName,
        final String longitudeDimensionName
    ) {
        return new StaticNetCdfGridReaderFactory<>(ncFilePath, latitudeDimensionName, longitudeDimensionName);
    }

    /**
     * @param modelGrid              factory for the grid the NetCDF file must align with
     * @param staticNetCdfGridReader factory for the supplier of the reader to read from
     * @return a {@link uk.ac.ox.poseidon.core.RelativeScopeFactory} for a map from NetCDF
     * variable name to the corresponding {@link DoubleGrid}
     * @see StaticGridsFromNetCdfFactory
     */
    public static <S extends Scope> StaticGridsFromNetCdfFactory<S> staticGridsFromNetCdf(
        final Factory<? super S, ? extends ModelGrid> modelGrid,
        final Factory<? super S, ? extends Supplier<StaticNetCdfGridReader>> staticNetCdfGridReader
    ) {
        return new StaticGridsFromNetCdfFactory<>(modelGrid, staticNetCdfGridReader);
    }

}
