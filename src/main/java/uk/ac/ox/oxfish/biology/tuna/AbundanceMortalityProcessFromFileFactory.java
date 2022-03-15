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

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFishState;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class AbundanceMortalityProcessFromFileFactory
    implements AlgorithmFactory<AbundanceMortalityProcess> {

    private Path mortalityFile;
    private Set<String> sources;
    private SpeciesCodes speciesCodes;
    private final CacheByFishState<Map<Species, Map<String, List<List<Double>>>>> cache =
        new CacheByFishState<>(this::load);
    @SuppressWarnings("unused")
    public AbundanceMortalityProcessFromFileFactory() {
    }
    public AbundanceMortalityProcessFromFileFactory(
        final Path mortalityFile,
        final Iterable<String> sources
    ) {
        this.mortalityFile = checkNotNull(mortalityFile);
        this.sources = ImmutableSet.copyOf(sources);
    }

    @SuppressWarnings("unused")
    public Path getMortalityFile() {
        return mortalityFile;
    }

    @SuppressWarnings("unused")
    public void setMortalityFile(final Path mortalityFile) {
        this.mortalityFile = mortalityFile;
    }

    public Set<String> getSources() {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        return sources;
    }

    public void setSources(final Set<String> sources) {
        //noinspection AssignmentOrReturnOfFieldWithMutableType
        this.sources = sources;
    }

    public SpeciesCodes getSpeciesCodes() {
        return speciesCodes;
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    @Override
    public AbundanceMortalityProcess apply(final FishState fishState) {
        return new AbundanceMortalityProcess(cache.get(fishState));
    }

    private Map<Species, Map<String, List<List<Double>>>> load(final FishState fishState) {
        checkNotNull(speciesCodes, "need to call setSpeciesCodes() before using");
        checkNotNull(sources);
        checkNotNull(mortalityFile);
        return parseAllRecords(mortalityFile)
            .stream()
            .filter(r -> sources.contains(r.getString("source")))
            .collect(groupingBy(
                r -> speciesCodes.getSpeciesFromCode(
                    fishState.getBiology(),
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
