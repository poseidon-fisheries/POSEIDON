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

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

public class SmallLargeAllocationGridsSupplier
    extends AbstractAllocationGridsSupplier<SmallLargeAllocationGridsSupplier.Key> {

    private static final Map<String, SizeGroup> groups = Arrays
        .stream(SizeGroup.values())
        .collect(toImmutableMap(
            SizeGroup::getCode,
            identity()
        ));

    @SuppressWarnings("SameParameterValue")
    SmallLargeAllocationGridsSupplier(
        final Path gridsFilePath,
        final MapExtent mapExtent,
        final int period
    ) {
        super(gridsFilePath, mapExtent, period, true);
    }

    @Override
    Key extractKeyFromRecord(
        final Record record
    ) {
        final String groupCode = record.getString("group");
        final String speciesCode = record.getString("species_code");
        return new Key(
            speciesCode,
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

        String getCode() {
            return code;
        }
    }

    static class Key extends Reallocator.SpeciesKey {

        private final SizeGroup sizeGroup;

        Key(
            final String speciesCode,
            final SizeGroup sizeGroup
        ) {
            super(speciesCode);
            this.sizeGroup = sizeGroup;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            final Key key = (Key) o;
            return sizeGroup == key.sizeGroup;
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), sizeGroup);
        }
    }

}
