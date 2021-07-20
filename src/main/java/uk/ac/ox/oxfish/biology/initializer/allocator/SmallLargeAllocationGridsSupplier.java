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
import uk.ac.ox.oxfish.biology.initializer.allocator.SmallLargeAllocationGridsSupplier.SizeGroup;
import uk.ac.ox.oxfish.geography.MapExtent;

class SmallLargeAllocationGridsSupplier
    extends AbstractAllocationGridsSupplier<Entry<String, SizeGroup>> {

    private static final Map<String, SizeGroup> groups = Arrays
        .stream(SizeGroup.values())
        .collect(toImmutableMap(
            SizeGroup::getCode,
            identity()
        ));

    SmallLargeAllocationGridsSupplier(
        final Path speciesCodesFilePath,
        final Path gridsFilePath,
        final MapExtent mapExtent
    ) {
        super(speciesCodesFilePath, gridsFilePath, mapExtent);
    }

    @Override
    Entry<String, SizeGroup> extractKeyFromRecord(
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

    public enum SizeGroup {

        SMALL("SML"), LARGE("LRG");

        private final String code;

        SizeGroup(final String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

}
