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

package uk.ac.ox.oxfish.utility;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import uk.ac.ox.oxfish.biology.Species;

public enum GrowthLogger {

    INSTANCE;
    final Builder<Object[]> builder = ImmutableList.builder();

    public void add(
        final Species species,
        final double biomassToUse,
        final double totalCapacity,
        final double malthusianParameter,
        final double newBiomass
    ) {
        builder.add(new Object[] {
            species,
            biomassToUse,
            totalCapacity,
            malthusianParameter,
            newBiomass
        });
    }

    public void writeToFile(final File outputFile) {
        try (final FileWriter fileWriter = new FileWriter(outputFile)) {
            final CsvWriter writer = new CsvWriter(fileWriter, new CsvWriterSettings());
            writer.writeHeaders("species",
                "biomassToUse",
                "totalCapacity",
                "malthusianParameter",
                "newBiomass");
            writer.writeRowsAndClose(builder.build());
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

}
