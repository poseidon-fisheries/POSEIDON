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

package uk.ac.ox.oxfish.biology.tuna;

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;
import uk.ac.ox.poseidon.common.core.parameters.InputPath;

import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.*;
import static uk.ac.ox.poseidon.common.core.csv.CsvParserUtil.recordStream;

public class AbundanceMortalityProcessFromFileFactory
    implements AlgorithmFactory<AbundanceMortalityProcess> {

    private InputPath mortalityFile;
    private List<String> sources;
    private final CacheByFishState<Map<Species, Map<String, List<List<Double>>>>> cache =
        new CacheByFishState<>(this::load);

    @SuppressWarnings("unused")
    public AbundanceMortalityProcessFromFileFactory() {
    }

    public AbundanceMortalityProcessFromFileFactory(
        final InputPath mortalityFile,
        final Iterable<String> sources
    ) {
        this.mortalityFile = checkNotNull(mortalityFile);
        this.sources = ImmutableList.copyOf(sources);
    }

    @SuppressWarnings("unused")
    public InputPath getMortalityFile() {
        return mortalityFile;
    }

    @SuppressWarnings("unused")
    public void setMortalityFile(final InputPath mortalityFile) {
        this.mortalityFile = mortalityFile;
    }

    public List<String> getSources() {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        return sources;
    }

    public void setSources(final List<String> sources) {
        // noinspection AssignmentOrReturnOfFieldWithMutableType
        this.sources = sources;
    }

    @Override
    public AbundanceMortalityProcess apply(final FishState fishState) {
        return new AbundanceMortalityProcess(cache.get(fishState));
    }

    private Map<Species, Map<String, List<List<Double>>>> load(final FishState fishState) {
        checkNotNull(sources);
        checkNotNull(mortalityFile);
        return recordStream(mortalityFile.get())
            .filter(r -> sources.contains(r.getString("source")))
            .collect(groupingBy(
                r -> fishState.getBiology().getSpeciesByCode(
                    r.getString("species_code")
                ),
                groupingBy(
                    r -> r.getString("source"),
                    collectingAndThen(
                        groupingBy(
                            r -> r.getString("sex"),
                            collectingAndThen(
                                toList(),
                                rs -> rs
                                    .stream()
                                    .sorted(comparingInt(r -> r.getInt("bin")))
                                    .map(r -> r.getDouble("mortality"))
                                    .collect(toList())
                            )
                        ),
                        map -> ImmutableList.of(map.get("male"), map.get("female"))
                    )
                )
            ));
    }
}
