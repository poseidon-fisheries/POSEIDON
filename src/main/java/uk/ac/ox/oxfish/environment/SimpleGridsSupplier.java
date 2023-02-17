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

package uk.ac.ox.oxfish.environment;

import com.univocity.parsers.common.record.Record;
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.geography.MapExtent;

import java.nio.file.Path;

public class SimpleGridsSupplier extends AbstractGridsSupplier<String> {

    private final String keyName;

    public SimpleGridsSupplier(
            Path gridsFilePath,
            MapExtent mapExtent, String keyName) {
        this(gridsFilePath, mapExtent,
                365,
                false, keyName);
    }

    public SimpleGridsSupplier(
            Path gridsFilePath,
            MapExtent mapExtent,
            int period,
            boolean toNormalize, String keyName) {
        super(null, gridsFilePath, mapExtent,
                period,
                toNormalize);
        this.keyName = keyName;
    }

    @Override
    String extractKeyFromRecord(Record record) {
        return keyName;
    }
}
