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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import sim.util.Int2D;
import uk.ac.ox.poseidon.biology.species.Species;
import uk.ac.ox.poseidon.core.Factory;
import uk.ac.ox.poseidon.core.RelativeScopeFactory;
import uk.ac.ox.poseidon.core.scopes.Scope;
import uk.ac.ox.poseidon.geography.Coordinate;
import uk.ac.ox.poseidon.geography.grids.ModelGrid;
import uk.ac.ox.poseidon.geography.grids.NetCdfGridWrapper;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static uk.ac.ox.poseidon.core.utils.Utils.multiStringKey;

/**
 * Reads a NetCDF file holding, for each of a number of dates, one biomass raster per species,
 * on a grid that must align cell-for-cell with the given {@link ModelGrid} (no resampling is
 * performed; misalignment is an error). The resulting grids are immutable and meant to be
 * shared: this resolves to global scope when its input factories do (see
 * {@link RelativeScopeFactory}), so the whole map is typically computed once and reused across
 * every simulation started from the same loaded scenario.
 * <p>
 * Each NetCDF data variable name is split on the last occurrence of {@code separator} into a
 * species code and a life stage; a name containing no {@code separator} names a bare species
 * with no life stage. The resulting (code, lifeStage) pairs are matched against the configured
 * species list, strictly both ways: every configured species must have exactly one matching
 * variable and vice versa, or this throws. Configured species codes must not themselves contain
 * {@code separator}, since that would make a bare code indistinguishable from a code/lifeStage
 * pair.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DateIndexedBiomassGridsFromNetCdfFactory<S extends Scope>
    extends RelativeScopeFactory<S, ImmutableMap<LocalDate, ImmutableList<ImmutableBiomassGrid>>> {

    private static final double COORDINATE_EPSILON = 1e-6;

    private Factory<? super S, ? extends ModelGrid> modelGrid;
    private Factory<? super S, ? extends List<? extends Species>> species;
    private Factory<? super S, ? extends Path> ncFilePath;
    private String separator;

    @Override
    protected ImmutableMap<LocalDate, ImmutableList<ImmutableBiomassGrid>> newInstance(final S scope) {

        final ModelGrid modelGrid = this.modelGrid.get(scope);
        final List<? extends Species> configuredSpecies = this.species.get(scope);
        checkNoCodeContainsSeparator(configuredSpecies, separator);

        try (final NetCdfGridWrapper netCdfGridWrapper = new NetCdfGridWrapper(ncFilePath.get(scope))) {

            checkGridAlignment(netCdfGridWrapper, modelGrid);

            final Map<String, String> variableNamesBySpeciesKey =
                matchVariablesToSpecies(
                    netCdfGridWrapper.getDataVariableNames(),
                    configuredSpecies,
                    separator
                );

            final List<Long> epochDays = netCdfGridWrapper.getEpochDays();
            final Set<LocalDate> seenDates = new HashSet<>();

            return IntStream
                .range(0, epochDays.size())
                .boxed()
                .collect(toImmutableMap(
                    timeIndex -> {
                        final LocalDate date = LocalDate.ofEpochDay(epochDays.get(timeIndex));
                        checkState(
                            seenDates.add(date),
                            "Duplicate date %s at time index %s in %s",
                            date, timeIndex, ncFilePath.get(scope)
                        );
                        return date;
                    },
                    timeIndex -> configuredSpecies
                        .stream()
                        .map(currentSpecies -> {
                            final String variableName =
                                variableNamesBySpeciesKey.get(currentSpecies.getKey());
                            final double[][] values =
                                netCdfGridWrapper.readSlice(variableName, timeIndex);
                            return new ImmutableBiomassGrid(modelGrid, currentSpecies, values);
                        })
                        .collect(toImmutableList())
                ));
        }
    }

    /**
     * Validates that the NetCDF grid aligns exactly with the {@code ModelGrid}, comparing cell
     * <em>centers</em> (via {@link ModelGrid#toCoordinate}) rather than raw envelope/cellsize
     * values, since those disagree at the 1e-7-1e-9 level between an {@code .asc} grid header and
     * a NetCDF {@code geotransform} even for grids that describe the same raster.
     */
    private static void checkGridAlignment(
        final NetCdfGridWrapper netCdfGridWrapper,
        final ModelGrid modelGrid
    ) {
        checkState(
            netCdfGridWrapper.getLonDimensionSize() == modelGrid.getGridWidth()
                && netCdfGridWrapper.getLatDimensionSize() == modelGrid.getGridHeight(),
            "NetCDF grid is %sx%s but ModelGrid is %sx%s",
            netCdfGridWrapper.getLonDimensionSize(), netCdfGridWrapper.getLatDimensionSize(),
            modelGrid.getGridWidth(), modelGrid.getGridHeight()
        );
        final double[] longitudes = netCdfGridWrapper.getLongitudes();
        final double[] latitudes = netCdfGridWrapper.getLatitudes();
        for (int lonIndex = 0; lonIndex < longitudes.length; lonIndex++) {
            for (int latIndex = 0; latIndex < latitudes.length; latIndex++) {
                final Coordinate expected = modelGrid.toCoordinate(new Int2D(lonIndex, latIndex));
                checkState(
                    Math.abs(expected.lon - longitudes[lonIndex]) < COORDINATE_EPSILON
                        && Math.abs(expected.lat - latitudes[latIndex]) < COORDINATE_EPSILON,
                    "NetCDF cell (lonIndex=%s, latIndex=%s) at (%s, %s) does not align with " +
                        "ModelGrid cell (%s, %s) at (%s, %s)",
                    lonIndex, latIndex, longitudes[lonIndex], latitudes[latIndex],
                    lonIndex, latIndex, expected.lon, expected.lat
                );
            }
        }
    }

    /**
     * Rejects any configured species whose code contains {@code separator}: since variable
     * names are split on it unconditionally, such a code would be indistinguishable from a
     * code/lifeStage pair and could never be matched correctly.
     */
    private static void checkNoCodeContainsSeparator(
        final List<? extends Species> configuredSpecies,
        final String separator
    ) {
        final List<String> offendingCodes = configuredSpecies
            .stream()
            .map(Species::getCode)
            .filter(code -> code.contains(separator))
            .toList();
        checkState(
            offendingCodes.isEmpty(),
            "Configured species codes must not contain the separator %s, but found: %s",
            separator, offendingCodes
        );
    }

    /**
     * Matches NetCDF variable names to configured species, strictly both ways: every configured
     * species must have exactly one matching variable and vice versa. A mismatch is treated as a
     * data-integrity defect (the file and the configured species list are meant to describe the
     * same species set) and throws, rather than silently skipping or synthesizing a species.
     */
    private static Map<String, String> matchVariablesToSpecies(
        final List<String> variableNames,
        final List<? extends Species> configuredSpecies,
        final String separator
    ) {
        final Map<String, Species> unmatchedSpeciesByKey =
            configuredSpecies.stream().collect(toMap(Species::getKey, identity()));

        final Map<String, String> variableNamesBySpeciesKey = new HashMap<>();
        final List<String> unmatchedVariables = new ArrayList<>();

        for (final String variableName : variableNames) {
            final String speciesKey = speciesKeyOf(variableName, separator);
            if (unmatchedSpeciesByKey.remove(speciesKey) == null) {
                unmatchedVariables.add(variableName);
            } else {
                variableNamesBySpeciesKey.put(speciesKey, variableName);
            }
        }

        checkState(
            unmatchedVariables.isEmpty() && unmatchedSpeciesByKey.isEmpty(),
            "NetCDF variables with no matching configured species: %s; " +
                "configured species with no matching NetCDF variable: %s",
            unmatchedVariables, unmatchedSpeciesByKey.keySet()
        );

        return variableNamesBySpeciesKey;
    }

    /**
     * Splits a variable name into a (code, lifeStage) species key by splitting unconditionally
     * on the last occurrence of {@code separator}; a name with no {@code separator} at all is a
     * bare code with a {@code null} life stage.
     */
    private static String speciesKeyOf(
        final String variableName,
        final String separator
    ) {
        final int separatorIndex = variableName.lastIndexOf(separator);
        if (separatorIndex < 0) {
            return multiStringKey(variableName, null);
        }
        final String code = variableName.substring(0, separatorIndex);
        final String lifeStage = variableName.substring(separatorIndex + separator.length());
        return multiStringKey(code, lifeStage);
    }

}
