/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2025, University of Oxford.
 *
 * University of Oxford means the Chancellor, Masters and Scholars of the
 * University of Oxford, having an administrative office at Wellington
 * Square, Oxford OX1 2JD, UK.
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.ox.oxfish.experiments.tuna.abundance;

import uk.ac.ox.oxfish.model.data.AbundanceHistogrammer;
import uk.ac.ox.oxfish.utility.FishStateUtilities;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

public class BiologyCheck {

    private BiologyCheck() {
    }

    public static void main(final String[] args) throws IOException {

        final Path output = Paths.get("docs/20220208 noboats_tuna/biocheck/");


        FishStateUtilities.run("biocheck",
            output.resolve("allduds_scenario.yaml"),
            output,
            0L,
            Level.ALL.getName(),
            false,
            null, 2,
            -1, null,
            state -> state.registerStartable(new AbundanceHistogrammer()),
            null,
            null
        );


    }

}
