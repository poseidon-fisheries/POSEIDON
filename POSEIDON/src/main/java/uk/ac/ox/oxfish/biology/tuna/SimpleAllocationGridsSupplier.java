/*
 *     POSEIDON, an agent-based model of fisheries
 *     Copyright (C) 2022  CoHESyS Lab cohesys.lab@gmail.com
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package uk.ac.ox.oxfish.biology.tuna;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.poseidon.common.core.geography.MapExtent;

import java.nio.file.Path;

public class SimpleAllocationGridsSupplier extends AbstractAllocationGridsSupplier<String> {

    private final String keyName;

    public SimpleAllocationGridsSupplier(
        final Path gridsFilePath,
        final MapExtent mapExtent,
        final String keyName
    ) {
        this(
            gridsFilePath,
            mapExtent,
            365,
            false,
            keyName
        );
    }

    public SimpleAllocationGridsSupplier(
        final Path gridsFilePath,
        final MapExtent mapExtent,
        final int period,
        final boolean toNormalize,
        final String keyName
    ) {
        super(
            gridsFilePath,
            mapExtent,
            period,
            toNormalize
        );
        this.keyName = keyName;
    }

    @Override
    String extractKeyFromRecord(final Record record) {
        return keyName;
    }
}
