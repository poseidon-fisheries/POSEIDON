/*
 * POSEIDON, an agent-based model of fisheries
 * Copyright (C) 2021 CoHESyS Lab cohesys.lab@gmail.com
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.fisher.purseseiner.strategies.gear;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import uk.ac.ox.oxfish.fisher.strategies.gear.GearStrategy;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.EpoScenario;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class FadRefillGearStrategyFactory implements AlgorithmFactory<GearStrategy> {

    private Path maxFadDeploymentsFile = EpoScenario.INPUT_PATH.resolve("max_deployments.csv");
    private final LoadingCache<Path, Map<String, Integer>> cache =
        CacheBuilder.newBuilder().build(CacheLoader.from(this::readValues));

    private double fadCost = 1000;

    @SuppressWarnings("unused")
    public double getFadCost() {
        return fadCost;
    }

    @SuppressWarnings("unused")
    public void setFadCost(final double fadCost) {
        this.fadCost = fadCost;
    }

    private Map<String, Integer> readValues() {
        return parseAllRecords(maxFadDeploymentsFile)
            .stream()
            .collect(toImmutableMap(
                record -> record.getString("boat_id"),
                record -> record.getInt("max_deployments")
            ));
    }

    @SuppressWarnings("unused")
    public Path getMaxFadDeploymentsFile() {
        return maxFadDeploymentsFile;
    }

    public void setMaxFadDeploymentsFile(final Path maxFadDeploymentsFile) {
        this.maxFadDeploymentsFile = maxFadDeploymentsFile;
    }

    @Override
    public GearStrategy apply(final FishState fishState) {
        try {
            return new FadRefillGearStrategy(cache.get(maxFadDeploymentsFile), fadCost);
        } catch (final ExecutionException e) {
            throw new IllegalStateException(e);
        }
    }
}
