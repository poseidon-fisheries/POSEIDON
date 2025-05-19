/*
 * POSEIDON: an agent-based model of fisheries
 * Copyright (c) 2017-2025, University of Oxford.
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

package uk.ac.ox.oxfish.experiments;

import com.google.common.collect.Lists;
import uk.ac.ox.oxfish.model.BatchRunner;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by carrknight on 9/24/16.
 */
public class BatchTest {


    public static void main(String[] args) throws IOException {


        BatchRunner runner = new BatchRunner(
            Paths.get("inputs", "first_paper", "fronts.yaml"),
            2,
            Lists.newArrayList(
                "Average Cash-Flow",
                "Average Distance From Port"
            ),
            Paths.get("output", "batch"),
            null,
            0l,
            null
        );

        runner.run(null);
        runner.run(null);
        runner.run(null);

    }

}
