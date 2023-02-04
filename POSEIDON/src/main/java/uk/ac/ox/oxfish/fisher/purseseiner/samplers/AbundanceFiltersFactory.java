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

package uk.ac.ox.oxfish.fisher.purseseiner.samplers;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.stream.Collectors.groupingBy;
import static uk.ac.ox.oxfish.fisher.purseseiner.actions.ActionClass.getSetActionClass;
import static uk.ac.ox.oxfish.utility.csv.CsvParserUtil.recordStream;

import com.google.common.collect.ImmutableList;
import com.univocity.parsers.common.record.Record;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import uk.ac.ox.oxfish.biology.Species;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.fisher.equipment.gear.components.NonMutatingArrayFilter;
import uk.ac.ox.oxfish.fisher.purseseiner.actions.AbstractSetAction;
import uk.ac.ox.oxfish.model.FishState;
import uk.ac.ox.oxfish.utility.AlgorithmFactory;

public class AbundanceFiltersFactory implements AlgorithmFactory<
    Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>>
    > {

    private SpeciesCodes speciesCodes;
    private Path selectivityFilePath;

    /**
     * Empty constructor for YAML loading
     */
    public AbundanceFiltersFactory() {
    }

    public AbundanceFiltersFactory(final Path selectivityFilePath) {
        this.selectivityFilePath = selectivityFilePath;
    }

    public void setSpeciesCodes(final SpeciesCodes speciesCodes) {
        this.speciesCodes = speciesCodes;
    }

    public Path getSelectivityFilePath() {
        return selectivityFilePath;
    }

    public void setSelectivityFilePath(final Path selectivityFilePath) {
        this.selectivityFilePath = selectivityFilePath;
    }

    @Override
    public Map<Class<? extends AbstractSetAction<?>>, Map<Species, NonMutatingArrayFilter>> apply(
        final FishState fishState
    ) {
        checkNotNull(speciesCodes);
        return recordStream(selectivityFilePath)
            .collect(groupingBy(
                r -> getSetActionClass(r.getString("set_type")),
                groupingBy(
                    r -> speciesCodes.getSpeciesFromCode(
                        fishState.getBiology(),
                        r.getString("species_code")
                    )
                )
            ))
            .entrySet()
            .stream()
            .collect(toImmutableMap(
                Entry::getKey,
                entry1 -> entry1
                    .getValue()
                    .entrySet()
                    .stream()
                    .collect(toImmutableMap(
                        Entry::getKey,
                        entry2 -> makeFilter(entry2.getValue())
                    ))
            ));
    }

    private static NonMutatingArrayFilter makeFilter(final Collection<Record> records) {
        final List<Double> selectivities = records
            .stream()
            .map(r -> r.getDouble("selectivity"))
            .collect(toImmutableList());
        return new NonMutatingArrayFilter(ImmutableList.of(selectivities, selectivities));
    }
}
