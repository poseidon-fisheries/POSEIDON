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

package uk.ac.ox.oxfish.biology.initializer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.parseAllRecords;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.SpeciesCodesFromFileFactory;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer.Bin;
import uk.ac.ox.oxfish.biology.initializer.allocator.AbundanceReallocator;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class AbundanceInitializerFactory
    implements AlgorithmFactory<AbundanceInitializer> {

    private final LoadingCache<Path, Map<String, List<Bin>>> binsCache =
        CacheBuilder.newBuilder().build(
            CacheLoader.from(AbundanceInitializerFactory::binsPerSpecies)
        );

    private Path speciesCodesFilePath;
    private Path binsFilePath;

    private AbundanceReallocator abundanceReallocator;

    /**
     * Empty constructor to allow YAML instantiation.
     */
    @SuppressWarnings("unused")
    public AbundanceInitializerFactory() {

    }

    public AbundanceInitializerFactory(
        final Path speciesCodesFilePath,
        final Path binsFilePath
    ) {
        this.speciesCodesFilePath = speciesCodesFilePath;
        this.binsFilePath = binsFilePath;
    }

    private static Map<String, List<Bin>> binsPerSpecies(final Path binsFilePath) {
        return parseAllRecords(binsFilePath).stream()
            .collect(groupingBy(
                record -> record.getString("species_code")
            ))
            .entrySet().stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry -> ImmutableList
                    .sortedCopyOf(
                        comparingInt(record -> record.getInt("bin")),
                        entry.getValue()
                    )
                    .stream()
                    .map(record -> new Bin(
                        record.getDouble("number_of_females"),
                        record.getDouble("number_of_males"),
                        record.getDouble("female_weight"),
                        record.getDouble("male_weight"),
                        record.getDouble("female_length"),
                        record.getDouble("male_length"),
                        record.getDouble("maturity"),
                        record.getDouble("female_natural_mortality"),
                        record.getDouble("male_natural_mortality")
                    ))
                    .collect(toImmutableList())
            ));
    }

    @SuppressWarnings("unused")
    public Path getSpeciesCodesFilePath() {
        return speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public void setSpeciesCodesFilePath(final Path speciesCodesFilePath) {
        this.speciesCodesFilePath = speciesCodesFilePath;
    }

    @SuppressWarnings("unused")
    public Path getBinsFilePath() {
        return binsFilePath;
    }

    @SuppressWarnings("unused")
    public void setBinsFilePath(final Path binsFilePath) {
        this.binsFilePath = binsFilePath;
    }

    @Override
    public AbundanceInitializer apply(final FishState fishState) {
        checkNotNull(abundanceReallocator, "need to call setAbundanceReallocator() before using");
        return new AbundanceInitializer(
            new SpeciesCodesFromFileFactory(this.speciesCodesFilePath).get(),
            binsCache.getUnchecked(this.binsFilePath),
            abundanceReallocator
        );
    }

    public AbundanceReallocator getAbundanceReallocator() {
        return abundanceReallocator;
    }

    public void setAbundanceReallocator(AbundanceReallocator abundanceReallocator) {
        this.abundanceReallocator = abundanceReallocator;
    }
}
