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

package uk.ac.ox.oxfish.biology.initializer.allocator;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;
import static uk.ac.ox.oxfish.utility.FishStateUtilities.entry;

import com.univocity.parsers.common.record.Record;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.initializer.allocator.JuvenileMatureAllocationGridsFactory.AgeGroup;

class JuvenileMatureAllocationGridsFactory
    extends AbstractAllocationGridsFactory<Entry<String, AgeGroup>> {

    private static final Map<String, AgeGroup> groups = Arrays
        .stream(AgeGroup.values())
        .collect(toImmutableMap(
            AgeGroup::getCode,
            identity()
        ));

    JuvenileMatureAllocationGridsFactory(
        final Path speciesCodesFilePath,
        final Path gridsFilePath
    ) {
        super(speciesCodesFilePath, gridsFilePath);
    }

    @Override
    Entry<String, AgeGroup> extractKeyFromRecord(
        final SpeciesCodes speciesCodes,
        final Record record
    ) {
        final String groupCode = record.getString("group");
        final String speciesCode = record.getString("species_code");
        return entry(
            speciesCodes.getSpeciesName(speciesCode),
            Optional.ofNullable(groups.get(groupCode))
                .orElseThrow(() -> new IllegalStateException("Unknown group code: " + groupCode))
        );
    }

    public enum AgeGroup {

        JUVENILE("juv"), MATURE("adu");

        private final String code;

        AgeGroup(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

}
