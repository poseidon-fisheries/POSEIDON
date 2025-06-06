/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2021-2025, University of Oxford.
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

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear;

import com.google.common.collect.ImmutableMap;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.oxfish.utility.Dummyable;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;
import uk.ac.ox.poseidon.common.core.parameters.IntegerParameter;

import java.nio.file.Path;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class FadRefillGearStrategyFactory implements AlgorithmFactory<GearStrategy>, Dummyable {

    private final CacheByFile<Map<Integer, ImmutableMap<String, Integer>>> cache = new CacheByFile<>(this::readValues);
    private InputPath maxFadDeploymentsFile;
    private IntegerParameter targetYear;

    private double fadCost = 1000;

    @SuppressWarnings("unused")
    public FadRefillGearStrategyFactory() {
    }

    public FadRefillGearStrategyFactory(
        final IntegerParameter targetYear,
        final InputPath maxFadDeploymentsFile
    ) {
        this.targetYear = targetYear;
        this.maxFadDeploymentsFile = maxFadDeploymentsFile;
    }

    @SuppressWarnings("unused")
    public IntegerParameter getTargetYear() {
        return targetYear;
    }

    @SuppressWarnings("unused")
    public void setTargetYear(final IntegerParameter targetYear) {
        this.targetYear = targetYear;
    }

    @SuppressWarnings("unused")
    public double getFadCost() {
        return fadCost;
    }

    @SuppressWarnings("unused")
    public void setFadCost(final double fadCost) {
        this.fadCost = fadCost;
    }

    private Map<Integer, ImmutableMap<String, Integer>> readValues(final Path maxFadDeploymentsFile) {
        return recordStream(maxFadDeploymentsFile)
            .collect(
                groupingBy(
                    record -> record.getInt("year"),
                    toImmutableMap(
                        record -> record.getString("ves_no"),
                        record -> record.getInt("max_deployments")
                    )
                )
            );
    }

    @SuppressWarnings("unused")
    public InputPath getMaxFadDeploymentsFile() {
        return maxFadDeploymentsFile;
    }

    public void setMaxFadDeploymentsFile(final InputPath maxFadDeploymentsFile) {
        this.maxFadDeploymentsFile = maxFadDeploymentsFile;
    }

    @Override
    public GearStrategy apply(final FishState fishState) {
        return new FadRefillGearStrategy(
            cache.apply(maxFadDeploymentsFile.get()).get(targetYear.getValue()),
            fadCost
        );
    }

    @Override
    public void useDummyData(final InputPath dummyDataFolder) {
        maxFadDeploymentsFile = dummyDataFolder.path("dummy_max_deployments.csv");
    }
}
