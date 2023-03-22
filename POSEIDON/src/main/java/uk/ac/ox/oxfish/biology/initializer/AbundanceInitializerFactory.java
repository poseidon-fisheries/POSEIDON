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

import com.google.common.collect.ImmutableList;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.complicated.AbundanceLocalBiology;
import uk.ac.ox.oxfish.biology.initializer.AbundanceInitializer.Bin;
import uk.ac.ox.oxfish.biology.tuna.BiologyInitializerFactory;
import uk.ac.ox.oxfish.biology.tuna.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.biology.tuna.WeightGroups;
import uk.ac.ox.oxfish.fisher.purseseiner.caches.CacheByFile;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.model.scenario.InputPath;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

public class AbundanceInitializerFactory
    extends BiologyInitializerFactory<AbundanceLocalBiology> {

    private final CacheByFile<Map<String, List<Bin>>> binsCache =
        new CacheByFile<>(AbundanceInitializerFactory::binsPerSpecies);

    private InputPath binsFile;

    public AbundanceInitializerFactory(
        final InputPath binsFile,
        final Supplier<SpeciesCodes> speciesCodesSupplier
    ) {
        this.binsFile = binsFile;
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    private Supplier<SpeciesCodes> speciesCodesSupplier;
    private Map<String, WeightGroups> weightGroupsPerSpecies;

    /**
     * Empty constructor to allow YAML instantiation.
     */
    @SuppressWarnings("unused")
    public AbundanceInitializerFactory() {

    }

    public Supplier<SpeciesCodes> getSpeciesCodesSupplier() {
        return speciesCodesSupplier;
    }

    private static Map<String, List<Bin>> binsPerSpecies(final Path binsFilePath) {
        return recordStream(binsFilePath)
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
                        record.getDouble("maturity")
                    ))
                    .collect(toImmutableList())
            ));
    }

    /**
     * This is named `assign` instead of `set` to avoid confusing the GUI and having it try to build
     * a widget for a map it cannot build one for.
     */
    public void assignWeightGroupsPerSpecies(final Map<String, WeightGroups> weightGroupsPerSpecies) {
        this.weightGroupsPerSpecies = weightGroupsPerSpecies;
    }

    public void setSpeciesCodesSupplier(final Supplier<SpeciesCodes> speciesCodesSupplier) {
        this.speciesCodesSupplier = speciesCodesSupplier;
    }

    @SuppressWarnings("unused")
    public InputPath getBinsFile() {
        return binsFile;
    }

    @SuppressWarnings("unused")
    public void setBinsFile(final InputPath binsFile) {
        this.binsFile = binsFile;
    }

    @Override
    public AbundanceInitializer apply(final FishState fishState) {
        checkNotNull(getReallocator(), "need to call setAbundanceReallocator() before using");
        checkNotNull(weightGroupsPerSpecies, "need to call setWeightGroupsPerSpecies() before using");
        return new AbundanceInitializer(
            speciesCodesSupplier.get(),
            binsCache.apply(this.binsFile.get()),
            weightGroupsPerSpecies,
            getReallocator()
        );
    }

}
