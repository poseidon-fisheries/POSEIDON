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
import uk.ac.ox.oxfish.biology.SpeciesCodes;
import uk.ac.ox.oxfish.biology.tuna.Reallocator.SpeciesKey;
import uk.ac.ox.oxfish.geography.MapExtent;

import java.nio.file.Path;
import java.util.function.Supplier;

class AllocationGridsSupplier
    extends AbstractAllocationGridsSupplier<SpeciesKey> {

    AllocationGridsSupplier(
        final Supplier<SpeciesCodes> speciesCodesSupplier,
        final Path gridsFilePath,
        final MapExtent mapExtent,
        final int period
    ) {
        super(speciesCodesSupplier, gridsFilePath, mapExtent, period, true);
    }

    @Override
    SpeciesKey extractKeyFromRecord(
        final SpeciesCodes speciesCodes,
        final Record record
    ) {
        return new SpeciesKey(
            speciesCodes.getSpeciesName(record.getString("species_code"))
        );
    }

}
